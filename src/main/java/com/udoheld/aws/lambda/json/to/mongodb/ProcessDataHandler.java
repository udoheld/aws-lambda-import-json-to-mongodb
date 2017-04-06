/*
    Copyright 2017 the original author or authors.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this program. If not, see
    <http://www.gnu.org/licenses/>.
 */

package com.udoheld.aws.lambda.json.to.mongodb;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.udoheld.aws.lambda.json.to.mongodb.model.MongoSensorData;
import com.udoheld.iot.json.InputParser;
import com.udoheld.iot.json.api.DataHolder;
import com.udoheld.iot.json.api.SensorData;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Morphia;

import java.io.Closeable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import java.util.stream.Stream;

/**
 * Processes the input and stores it to MongoDB.
 *
 * @author Udo Held
 */
public class ProcessDataHandler implements Closeable {

  public static final int MAX_WRITE_ATTEMPTS = 10;

  private static MongoClient globalMongoClient;
  private MongoClient localMongoClient;
  private InputParser inputParser = new InputParser();
  private Morphia mongoMorphia;
  private AdvancedDatastore mongoDatastore;
  private final boolean createGlobalConnection;

  private ProcessDataHandler(String connectionUri, String database,
                             boolean createGlobalConnection) {
    this.createGlobalConnection = createGlobalConnection;
    init(connectionUri, database);
  }

  private void init(String connectionUri, String database) {
    MongoClient mongoClient = initConnection(connectionUri);

    mongoMorphia = new Morphia();
    mongoMorphia.map(MongoSensorData.class);
    mongoDatastore = (AdvancedDatastore) mongoMorphia.createDatastore(mongoClient, database);
  }

  private MongoClient initConnection(String connectionUri) {
    MongoClientURI uri = new MongoClientURI(connectionUri);
    MongoClient mongoClient;
    if (createGlobalConnection) {
      mongoClient =  initGlobalConnection(uri);
    } else {
      mongoClient = new MongoClient(uri);
      localMongoClient = mongoClient;
    }
    return mongoClient;
  }

  /**
   * Initiates a global MongoDB connection.
   * @param mongoUri mongoDB connection Uri
   * @return MongoDB connection
   */
  private synchronized MongoClient initGlobalConnection(MongoClientURI mongoUri) {
    if (globalMongoClient == null) {
      globalMongoClient = new MongoClient(mongoUri);
    }
    return globalMongoClient;
  }

  /**
   * Initiates the ProcessHandler for input processing.
   * @param connectionUri connectionUri
   * @param database databaseName
   * @param createGlobalConnection Creates a global connection that can be reused. The connection
   *                               will be closed if {@link #close} is called.
   * @return ProcessDataHandler.
   */
  public static ProcessDataHandler getProcessDataHandler(String connectionUri, String database,
                                                         boolean createGlobalConnection) {
    return new ProcessDataHandler(connectionUri, database, createGlobalConnection );
  }

  /**
   * Processes input message and stores it into the MongoDB database.
   * @param input JSON in limited SenML format.
   */
  public void processInput(String input) {
    SensorData [] sensorData = inputParser.parseInput(input);

    // device, type, date
    Map<String, Map<String, Map<LocalDate, MongoSensorData>>> sensorHolder = new HashMap<>();
    mergeSensorData(sensorData,sensorHolder);

    storeSensorData(sensorHolder);
  }

  private void storeSensorData(
      Map<String, Map<String, Map<LocalDate, MongoSensorData>>> sensorHolder) {
    sensorHolder.entrySet()
        .stream()
        .flatMap(x -> x.getValue().entrySet().stream())
        .flatMap(x -> x.getValue().entrySet().stream())
        .forEach(x -> storeRecord(x.getValue()));
  }

  private void storeRecord(MongoSensorData mongoSensorData) {
    int writeAttempts = 0;

    boolean writtenRecord = false;

    while (writeAttempts++ < MAX_WRITE_ATTEMPTS && ! writtenRecord ) {
      writtenRecord = attemptRecordWrite(mongoSensorData);
    }
    if (!writtenRecord) {
      throw new ConcurrentModificationException("Unable to write record to database probably due to"
          + " concurrent modification of the record. Giving up after " + MAX_WRITE_ATTEMPTS
          + " attempts.");
    }
  }

  private boolean attemptRecordWrite(MongoSensorData mongoSensorData) {
    MongoSensorData existingRecord
        = mongoDatastore.get(mongoSensorData.getClass(),mongoSensorData.getId());
    try {
      MongoSensorData srcRecord = (MongoSensorData) mongoSensorData.clone();

      MongoSensorData mergedRecord = mergeRecords(srcRecord,existingRecord);

      return writeRecord(mergedRecord, existingRecord == null);

    } catch (CloneNotSupportedException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  private boolean writeRecord(MongoSensorData record, boolean newRecord) {
    if (newRecord) {
      try {
        mongoDatastore.insert(record);
      } catch (DuplicateKeyException e) {
        return false;
      }
    } else {
      try {
        mongoDatastore.save(record);
      } catch (ConcurrentModificationException e) {
        return false;
      }
    }

    return true;
  }

  private MongoSensorData mergeRecords(MongoSensorData srcData, MongoSensorData existing) {

    final MongoSensorData target = existing == null ? srcData : existing;
    if (existing != null) {
      final Map<Integer,Map<Integer,Double>> currentDetail = target.getDetailed();
      BiFunction<Map<Integer,Double>,Map<Integer,Double>,Map<Integer,Double>> mergeHour =
          (input,mergeE) -> {
            input.entrySet()
                .stream()
                .forEach(entry -> mergeE.put(entry.getKey(),entry.getValue()));
            return mergeE;
          };

      BiConsumer<Integer,Map<Integer,Double>> mergeDetails =
          (hour,details) -> {
            if (currentDetail.putIfAbsent(hour,details) != null) {
              currentDetail.put(hour,mergeHour.apply(details,currentDetail.get(hour)));
            }
          };

      srcData.getDetailed()
          .entrySet()
          .stream()
          .forEach(x -> {
            mergeDetails.accept(x.getKey(),x.getValue());
          });
    }
    calculateSummary(target);
    return target;
  }

  private void calculateSummary(MongoSensorData record) {
    MongoSensorData.Summary summary = new MongoSensorData.Summary();
    Map<Integer,Double> average = new HashMap<>();

    Function<Map<Integer,Double>,Double> calcAvg = (minuteValues) -> {
      OptionalDouble optDouble = minuteValues.entrySet()
          .stream()
          .mapToDouble(y -> y.getValue())
          .average();
      return optDouble.isPresent() ? optDouble.getAsDouble() : null;
    };

    if (record.getDetailed() != null) {
      record.getDetailed()
          .entrySet()
          .stream()
          .forEach( x -> average.put(x.getKey(),calcAvg.apply(x.getValue())));
    }

    summary.setAverage(average);
    record.setSummary(summary);
  }

  private void mergeSensorData(SensorData[] sensorData,
                     Map<String, Map<String, Map<LocalDate, MongoSensorData>>> sensorHolder) {
    Stream.of(sensorData)
        .filter(x -> x.getData() != null)
        .flatMap(x -> Stream.of(x.getData()))
        .filter(this::validateMeasurement)
        .forEach(x -> mergeDevice(x,sensorHolder));
  }

  private boolean validateMeasurement(DataHolder dataHolder) {
    if (dataHolder.getBaseName() == null || dataHolder.getBaseName().isEmpty()
        || dataHolder.getName() == null || dataHolder.getName().isEmpty()
        || dataHolder.getBaseTimeStamp() == null || dataHolder.getValue() == null) {
      return false;
    }
    return true;
  }


  private void mergeDevice(DataHolder dataHolder,
                           Map<String, Map<String, Map<LocalDate, MongoSensorData>>> sensorHolder) {
    sensorHolder.putIfAbsent(dataHolder.getBaseName(),new HashMap<>());
    mergeType(dataHolder,sensorHolder.get(dataHolder.getBaseName()));
  }
  
  private void mergeType(DataHolder dataHolder,
                         Map<String, Map<LocalDate, MongoSensorData>> sensorHolder) {
    sensorHolder.putIfAbsent(dataHolder.getName(), new HashMap<>());
    mergeDate(dataHolder,sensorHolder.get(dataHolder.getName()));
  }

  private void mergeDate(DataHolder dataHolder, Map<LocalDate, MongoSensorData> documentMap) {
    LocalDate date = convertTimestampToDate(dataHolder.getBaseTimeStamp().longValue());
    documentMap.putIfAbsent(date,createMongoSensorData(dataHolder));
    mergeSensorDataMeasurement(dataHolder, documentMap.get(date));
  }

  private void mergeSensorDataMeasurement(DataHolder dataHolder, MongoSensorData sensorData) {
    LocalDateTime dateTime = convertTimestampToDateTime(dataHolder.getBaseTimeStamp().longValue());
    sensorData.getDetailed().putIfAbsent(dateTime.getHour(),new HashMap<>());
    Map<Integer,Double> hour = sensorData.getDetailed().get(dateTime.getHour());
    hour.put(dateTime.getMinute(),dataHolder.getValue());
  }

  private MongoSensorData createMongoSensorData(DataHolder dataHolder) {

    MongoSensorData.Id id = new MongoSensorData.Id();
    id.setDevice(dataHolder.getBaseName());
    id.setDate(convertTimestampToDate(dataHolder.getBaseTimeStamp().longValue()));
    id.setType(dataHolder.getName());

    MongoSensorData sensorData = new MongoSensorData();
    sensorData.setId(id);
    sensorData.setDetailed(new HashMap<>());
    return sensorData;
  }

  /**
   * Converts a unix timestamp to a UTC LocalDate.
   * @param timestamp unix timestamp
   * @return LocalDate based on UTC.
   */
  public static LocalDate convertTimestampToDate(long timestamp) {
    LocalDate date = Instant.ofEpochSecond(timestamp).atZone(ZoneId.of("UTC")).toLocalDate();
    return date;
  }

  /**
   * Converts a unix timestamp to a UTC LocalDateTime.
   * @param timestamp unix timestamp
   * @return LocalDateTime based on UTC.
   */
  public static LocalDateTime convertTimestampToDateTime(long timestamp) {
    LocalDateTime dateTime
        = Instant.ofEpochSecond(timestamp).atZone(ZoneId.of("UTC")).toLocalDateTime();
    return dateTime;
  }


  @Override
  public void close() {
    if (createGlobalConnection) {
      if (globalMongoClient != null ) {
        globalMongoClient.close();
      }
    } else {
      if (localMongoClient != null) {
        localMongoClient.close();
      }
    }
  }
}

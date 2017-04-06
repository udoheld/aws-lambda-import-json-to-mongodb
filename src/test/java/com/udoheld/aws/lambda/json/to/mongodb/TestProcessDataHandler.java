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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.udoheld.aws.lambda.json.to.mongodb.model.MongoSensorData;
import com.udoheld.iot.json.InputParser;
import com.udoheld.iot.json.api.SensorData;
import org.bson.conversions.Bson;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Udo Held
 */
public class TestProcessDataHandler {

  private String mongoDbConnectionUrl = "mongodb://localhost/?connectTimeoutMS=2500&socketTimeoutMS=5000";
  private String mongoDbDatabase = "unitTest";
  private String mongoDbCollection = "sensorData";

  private String readTestFile() throws IOException {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("test.json");
    assertNotNull(is);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      sb.append(line);
    }

    return sb.toString();
  }

  private SensorData [] readTestData() throws IOException {
    InputParser inputParser = new InputParser();
    return inputParser.parseInput(readTestFile());
  }

  @Test
  public void processTestDataRecord() throws IOException, NoSuchMethodException,
      InvocationTargetException, IllegalAccessException {
    SensorData[] sensorData = readTestData();
    assertNotNull(sensorData);
    assertTrue(sensorData.length > 0);

    Map<String, Map<String, Map<LocalDate, MongoSensorData>>> sensorHolder = new HashMap<>();

    try (ProcessDataHandler dataHandler
             = ProcessDataHandler.getProcessDataHandler (mongoDbConnectionUrl,mongoDbDatabase,
        true)) {
      Method method = dataHandler.getClass().getDeclaredMethod("mergeSensorData",
          sensorData.getClass(),Map.class);
      method.setAccessible(true);
      method.invoke(dataHandler,sensorData,sensorHolder);

      assertTrue(sensorHolder.size() == 2);
      assertTrue(sensorHolder.containsKey("urn:dev:mac:784b87a58c3d;temp1"));
      assertTrue(sensorHolder.get("urn:dev:mac:784b87a58c3d;temp1").size() == 2);

      assertTrue(sensorHolder.containsKey("urn:dev:mac:784b87a58c3d;temp2"));
      assertTrue(sensorHolder.get("urn:dev:mac:784b87a58c3d;temp2").size() == 1);
    }
  }

  @Test
  public void processTestData() throws IOException {
    MongoClientURI uri = new MongoClientURI(mongoDbConnectionUrl);
    try (MongoClient mongoClient = new MongoClient(uri)) {
      MongoDatabase mongoDatabase = mongoClient.getDatabase(mongoDbDatabase);
      MongoCollection mongoCollection = mongoDatabase.getCollection(mongoDbCollection);
      mongoCollection.drop();

      try ( ProcessDataHandler dataHandler
                = ProcessDataHandler.getProcessDataHandler(mongoDbConnectionUrl, mongoDbDatabase,
                false)) {
        dataHandler.processInput(readTestFile());
      }
      assertEquals(6l, mongoCollection.count());
      Bson filter = combine(eq("_id.device","urn:dev:mac:784b87a58c3d;temp2"),
          eq("_id.type","temp"));
      assertEquals(1l, mongoCollection.count(filter));
    }
  }

  @Test
  public void testMergeRecords() throws NoSuchMethodException, InvocationTargetException,
      IllegalAccessException {
    MongoSensorData srcData = new MongoSensorData();
    srcData.setDetailed(new HashMap<>());
    srcData.getDetailed().put(1,new HashMap<>());
    srcData.getDetailed().get(1).put(1,1.0);
    srcData.getDetailed().get(1).put(2,2.0);
    srcData.getDetailed().put(2,new HashMap<>());
    srcData.getDetailed().get(2).put(3,3.0);

    MongoSensorData existingData = new MongoSensorData();
    existingData.setDetailed(new HashMap<>());
    existingData.getDetailed().put(1, new HashMap<>());
    existingData.getDetailed().get(1).put(1,1.5);
    existingData.getDetailed().get(1).put(4,4.0);
    existingData.getDetailed().put(5, new HashMap<>());
    existingData.getDetailed().get(5).put(5,5.0);

    try (ProcessDataHandler dataHandler
             = ProcessDataHandler.getProcessDataHandler(mongoDbConnectionUrl, mongoDbDatabase,
        false)) {
      Method method = dataHandler.getClass().getDeclaredMethod("mergeRecords",
          srcData.getClass(), existingData.getClass());
      method.setAccessible(true);
      MongoSensorData merged = (MongoSensorData) method.invoke(dataHandler, srcData, existingData);

      assertNotNull(merged);
      assertNotNull(merged.getDetailed());
      assertTrue(merged.getDetailed().containsKey(1));
      assertTrue(merged.getDetailed().containsKey(2));
      assertTrue(merged.getDetailed().containsKey(5));

      assertEquals(1.0, existingData.getDetailed().get(1).get(1).byteValue(), 0.0);
      assertEquals(2.0, existingData.getDetailed().get(1).get(2).byteValue(), 0.0);
      assertEquals(4.0, existingData.getDetailed().get(1).get(4).byteValue(), 0.0);
      assertEquals(3.0, existingData.getDetailed().get(2).get(3).byteValue(), 0.0);
      assertEquals(5.0, existingData.getDetailed().get(5).get(5).byteValue(), 0.0);
    }
  }
}

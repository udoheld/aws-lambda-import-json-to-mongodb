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

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.udoheld.aws.lambda.json.to.mongodb.model.MongoSensorData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.time.LocalDate;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Udo Held
 */
public class TestMorphia {
  private String mongoDbConnectionUrl = "mongodb://localhost/?connectTimeoutMS=2500&socketTimeoutMS=5000";
  private String mongoDbDatabase = "unitTest";
  private MongoClient mongoClient;

  private Morphia morphia;
  private Datastore datastore;

  @Before
  public void init() {
    initMorphia();
  }

  public void initMorphia() {

    MongoClientURI uri = new MongoClientURI(mongoDbConnectionUrl);
    mongoClient = new MongoClient(uri);

    morphia = new Morphia();

    morphia.mapPackage(MongoSensorData.class.getPackage().getName());
    morphia.getMapper().getConverters().addConverter(new IsoDateConverter());
    morphia.mapPackageFromClass(LocalDate.class);

    // create the Datastore connecting to the default port on the local host
    datastore = morphia.createDatastore(mongoClient, mongoDbDatabase);
  }

  @After
  public void destroy() {
    if (mongoClient != null) {
      mongoClient.close();
    }
  }

  @Test
  public void testReadWrite() {
    String device = "testReadWrite";
    String type = "testType";

    Query<MongoSensorData> query = datastore.createQuery(MongoSensorData.class);
    datastore.delete(query);

    LocalDate curDate = LocalDate.now();

    MongoSensorData sensorData = new MongoSensorData();
    MongoSensorData.Id id = new MongoSensorData.Id();
    id.setDate(curDate);
    id.setDevice(device);
    id.setType(type);
    sensorData.setId(id);

    sensorData.setDetailed(new HashMap<>());
    Map<Integer,Double> hourOne = new HashMap<>();
    hourOne.put(4,13.0);
    hourOne.put(5,13.5);
    hourOne.put(6,14.25);
    Map<Integer,Double> hourFive = new HashMap<>();
    hourFive.put(8,15.0);
    hourFive.put(9,15.5);
    hourFive.put(10,15.75);
    sensorData.getDetailed().put(1,hourOne);
    sensorData.getDetailed().put(5,hourFive);

    datastore.save(sensorData);

    MongoSensorData readData = datastore.get(sensorData.getClass(),sensorData.getId());
    assertTrue(sensorData.equals(readData));

    datastore.delete(sensorData);
    readData = datastore.get(sensorData.getClass(),readData.getId());
    assertNull(readData);
  }

  @Test
  public void testDuplicateInsert() {
    String device = "testDuplicateInsert";
    String type = "testType";

    Query<MongoSensorData> query = datastore.createQuery(MongoSensorData.class);
    datastore.delete(query);

    LocalDate curDate = LocalDate.now();

    MongoSensorData sensorData = new MongoSensorData();
    MongoSensorData.Id id = new MongoSensorData.Id();
    id.setDate(curDate);
    id.setDevice(device);
    id.setType(type);
    sensorData.setId(id);

    AdvancedDatastore ads = (AdvancedDatastore) datastore;

    ads.insert(sensorData);

    try {
      ads.insert(sensorData);
      fail("no duplicate key exception");
    } catch (DuplicateKeyException expected) {
    } finally {
      datastore.delete(query);
    }
  }

  @Test
  public void testUpdate() {
    String device = "testUpdate";
    String type = "testType";

    Query<MongoSensorData> query = datastore.createQuery(MongoSensorData.class);
    datastore.delete(query);

    LocalDate curDate = LocalDate.now();

    MongoSensorData sensorData = new MongoSensorData();
    MongoSensorData.Id id = new MongoSensorData.Id();
    id.setDate(curDate);
    id.setDevice(device);
    id.setType(type);
    sensorData.setId(id);

    datastore.save(sensorData);

    sensorData.setDetailed(new HashMap<>());
    sensorData.getDetailed().put(0,new HashMap<>());
    sensorData.getDetailed().get(0).put(1,1.0);

    datastore.save(sensorData);

    MongoDatabase mongoDb = mongoClient.getDatabase(mongoDbDatabase);
    MongoCollection mongoCol = mongoDb.getCollection(datastore.getCollection(sensorData.getClass()).getName());
    BasicDBObject updateOp = new BasicDBObject().append("$inc", new BasicDBObject().append("version", 1));
    UpdateResult updateResult = mongoCol.updateMany(eq("_id.device",device),updateOp);
    assertTrue(updateResult.getModifiedCount() > 0);

    sensorData.getDetailed().get(0).put(2,2.0);
    try {
      datastore.save(sensorData);
      fail("ConcurrentModificationException was expected.");
    } catch (ConcurrentModificationException expected){
    }
  }
}

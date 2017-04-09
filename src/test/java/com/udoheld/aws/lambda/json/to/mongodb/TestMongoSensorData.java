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

import com.udoheld.aws.lambda.json.to.mongodb.model.MongoSensorData;
import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Testing the MongoSensorData methods.
 * @author Udo Held
 */
public class TestMongoSensorData {

  @Test
  public void testEquals(){
    MongoSensorData data = new MongoSensorData();
    assertFalse(data.equals(null));
    assertFalse(data.equals(new MongoSensorData.Id()));

    data.setDetailed(new HashMap<>());
    data.getDetailed().put(1, new HashMap<>());
    data.getDetailed().get(1).put(1,1.0);

    MongoSensorData data2 = new MongoSensorData();
    data2.setDetailed(new HashMap<>());
    data2.getDetailed().put(1, new HashMap<>());

    assertFalse(data.equals(data2));
    data2.getDetailed().get(1).put(1,1.0);
    assertTrue(data.equals(data2));

    data.setSummary(new MongoSensorData.Summary());
    data.getSummary().setAverage(new HashMap<>());
    data.getSummary().getAverage().put(1,1.0);
    assertFalse(data.equals(data2));

    data2.setSummary(new MongoSensorData.Summary());
    data2.getSummary().setAverage(new HashMap<>());
    data2.getSummary().getAverage().put(1,1.0);
    assertTrue(data.equals(data2));

    data.getSummary().getAverage().put(2,2.0);
    assertFalse(data.equals(data2));
    data2.getSummary().getAverage().put(2,2.0);

    data.setVersion(1L);
    assertFalse(data.equals(data2));

    data2.setVersion(1L);
    assertTrue(data.equals(data2));

    LocalDate testDate = LocalDate.now();
    data.setId(new MongoSensorData.Id());
    data.getId().setDate(testDate);
    assertFalse(data.equals(data2));

    data2.setId(new MongoSensorData.Id());
    data2.getId().setDate(testDate);
    assertTrue(data.equals(data2));

    data.getId().setDevice("service");
    data.getId().setType("type");
    data2.getId().setDevice("service");
    data2.getId().setType("type");
    assertTrue(data.equals(data2));

    assertTrue(data.equals(data));
  }

  @Test
  public void testClone() throws CloneNotSupportedException {
    MongoSensorData data = new MongoSensorData();
    MongoSensorData clone = (MongoSensorData) data.clone();
    assertTrue(data.equals(clone));

    data.setId(new MongoSensorData.Id());
    data.getId().setDate(LocalDate.now());
    data.getId().setDevice("service");
    data.getId().setType("type");
    clone = (MongoSensorData) data.clone();
    assertTrue(data.equals(clone));

    data.setDetailed(new HashMap<>());
    data.getDetailed().put(1, new HashMap<>());
    data.getDetailed().get(1).put(1,1.0);
    clone = (MongoSensorData) data.clone();
    assertTrue(data.equals(clone));

    data.setSummary(new MongoSensorData.Summary());
    data.getSummary().setAverage(new HashMap<>());
    data.getSummary().getAverage().put(1,1.0);

    clone = (MongoSensorData) data.clone();
    assertTrue(data.equals(clone));

    data.setVersion(1L);
    clone = (MongoSensorData) data.clone();
    assertTrue(data.equals(clone));
  }
}

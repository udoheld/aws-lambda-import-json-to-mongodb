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

package com.udoheld.aws.lambda.json.to.mongodb.model;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Version;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Object mapping class for Morphia.
 *
 * @author Udo Held
 */
@Entity(value = "sensorData",noClassnameStored = true)
public class MongoSensorData implements Cloneable {
  @org.mongodb.morphia.annotations.Id
  private MongoSensorData.Id id;

  private Map<Integer,Map<Integer,Double>> detailed;

  private Summary summary;

  @Version
  private Long version;

  public Id getId() {
    return id;
  }

  public void setId(Id id) {
    this.id = id;
  }

  public Map<Integer, Map<Integer, Double>> getDetailed() {
    return detailed;
  }

  public void setDetailed(Map<Integer, Map<Integer, Double>> detailed) {
    this.detailed = detailed;
  }

  public Summary getSummary() {
    return summary;
  }

  public void setSummary(Summary summary) {
    this.summary = summary;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    MongoSensorData clone = (MongoSensorData) super.clone();
    if (id != null) {
      clone.setId((Id) id.clone());
    }
    if (summary != null) {
      clone.setSummary((Summary) summary.clone());
    }
    clone.setVersion(version);
    if (detailed != null) {
      Map<Integer,Map<Integer,Double>> clonedDetailed = new HashMap<>();

      Function<Map<Integer,Double>,Map<Integer,Double>> cloneDetailedEntry = (inputMap) -> {
        if (inputMap == null) {
          return null;
        }
        Map<Integer,Double> clonedMap = new HashMap<>();
        inputMap.entrySet()
            .stream()
            .forEach(y -> clonedMap.put(y.getKey() == null ? null : new Integer(y.getKey()),
                y.getValue() == null ? null : new Double(y.getValue())));
        return clonedMap;
      };

      detailed.entrySet()
          .stream()
          .forEach(x -> clonedDetailed.put(x.getKey() == null ? null : new Integer(x.getKey()),
              x.getValue() == null ? null : cloneDetailedEntry.apply(x.getValue())));
    }

    return clone;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    MongoSensorData data = (MongoSensorData) obj;

    if ((detailed == null && data.getDetailed() != null)
        || (detailed != null && ! detailed.equals(data.getDetailed()))) {
      return false;
    }

    if ((id == null && data.getId() != null) || (id != null && ! id.equals(data.getId()))) {
      return false;
    }

    if ((summary == null && data.getSummary() != null)
        || (summary != null && ! summary.equals(data.getSummary()))) {
      return false;
    }

    if ((version == null && data.getVersion() != null)
        || (version != null && ! version.equals(data.getVersion()))) {
      return false;
    }


    return true;
  }

  @Embedded
  public static class Id implements Cloneable {
    private String device;
    private LocalDate date;
    private String type;

    public String getDevice() {
      return device;
    }

    public void setDevice(String device) {
      this.device = device;
    }

    public LocalDate getDate() {
      return date;
    }

    public void setDate(LocalDate date) {
      this.date = date;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      Id id = (Id) obj;

      if ((date == null && id.getDate() != null)
          || (date != null && !date.equals(id.getDate()))) {
        return false;
      }

      if ((device == null && id.getDevice() != null)
          || (device != null && !device.equals(id.getDevice()))) {
        return false;
      }

      if ((type == null && id.getType() != null)
          || (type != null && !type.equals(id.getType()))) {
        return false;
      }

      return true;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
      Id clone = (Id) super.clone();
      clone.setDevice(device);
      clone.setDate(date);
      clone.setType(type);
      return clone;
    }
  }

  @Embedded
  public static class Summary implements Cloneable {
    @Property("avg")
    private Map<Integer,Double> average;

    @Override
    public Object clone() throws CloneNotSupportedException {
      Summary clone = (Summary) super.clone();
      if (average != null) {
        Map<Integer,Double> clonedAverage = new HashMap<>();
        average.entrySet()
            .stream()
            .forEach(x -> clonedAverage.put(
                new Integer(x.getKey()),x.getValue() == null ? null : new Double(x.getValue())));
        clone.setAverage(clonedAverage);
      }
      return clone;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      Summary summary = (Summary) obj;
      if ((this.getAverage() == null && summary.getAverage() != null)
          || (this.getAverage() != null && ! this.getAverage().equals(summary.getAverage()))) {
        return false;
      }
      return true;
    }

    public Map<Integer, Double> getAverage() {
      return average;
    }

    public void setAverage(Map<Integer, Double> average) {
      this.average = average;
    }
  }
}

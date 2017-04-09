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

package com.udoheld.aws.lambda.json.to.mongodb.cfg;

/**
 * This Pojo holds the runtime configuration.
 * @author Udo Held
 */
public class Config {
  private boolean debug;
  private boolean debugInput;
  private boolean disableSnsRemoval;
  private boolean localtest;

  private String mongoDbUsername;
  private String mongoDbPassword;
  private String mongoDbHosts;
  private String mongoDbDatabase;
  private String mongoDbOptions;
  private boolean mongoDbKeepConnection;

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public boolean isDebugInput() {
    return debugInput;
  }

  public void setDebugInput(boolean debugInput) {
    this.debugInput = debugInput;
  }

  public boolean isDisableSnsRemoval() {
    return disableSnsRemoval;
  }

  public void setDisableSnsRemoval(boolean disableSnsRemoval) {
    this.disableSnsRemoval = disableSnsRemoval;
  }

  public boolean isLocaltest() {
    return localtest;
  }

  public void setLocaltest(boolean localtest) {
    this.localtest = localtest;
  }

  public String getMongoDbUsername() {
    return mongoDbUsername;
  }

  public void setMongoDbUsername(String mongoDbUsername) {
    this.mongoDbUsername = mongoDbUsername;
  }

  public String getMongoDbPassword() {
    return mongoDbPassword;
  }

  public void setMongoDbPassword(String mongoDbPassword) {
    this.mongoDbPassword = mongoDbPassword;
  }

  public String getMongoDbHosts() {
    return mongoDbHosts;
  }

  public void setMongoDbHosts(String mongoDbHosts) {
    this.mongoDbHosts = mongoDbHosts;
  }

  public String getMongoDbDatabase() {
    return mongoDbDatabase;
  }

  public void setMongoDbDatabase(String mongoDbDatabase) {
    this.mongoDbDatabase = mongoDbDatabase;
  }

  public String getMongoDbOptions() {
    return mongoDbOptions;
  }

  public void setMongoDbOptions(String mongoDbOptions) {
    this.mongoDbOptions = mongoDbOptions;
  }

  public boolean isMongoDbKeepConnection() {
    return mongoDbKeepConnection;
  }

  public void setMongoDbKeepConnection(boolean mongoDbKeepConnection) {
    this.mongoDbKeepConnection = mongoDbKeepConnection;
  }
}

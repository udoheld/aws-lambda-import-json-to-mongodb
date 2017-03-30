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

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCursor;
import com.udoheld.aws.lambda.json.to.mongodb.cfg.Config;
import com.udoheld.aws.lambda.json.to.mongodb.cfg.ConfigurationInitializer;

import java.util.Map;

/**
 * Tests the connection to MongoDB.
 *
 * @author Udo Held
 */
public class MongoConnectionTest implements RequestHandler<Map<String,Object>,String> {
  @Override
  public String handleRequest(Map<String, Object> stringObjectMap, Context context) {
    LambdaLogger logger = context.getLogger();
    logger.log("Pre opening connection.");

    Config config = ConfigurationInitializer.initializeConfig(context,true);

    String connectionUri = ConfigurationInitializer.buildConnectionUri(config);

    MongoClientURI uri = new MongoClientURI(connectionUri);

    try (MongoClient mongoClient = new MongoClient(uri);
         MongoCursor cursor = mongoClient.listDatabases().iterator()) {
      logger.log("Opened connection");
      StringBuilder sb = new StringBuilder();

      cursor.forEachRemaining(database -> sb.append(database + " "));

      logger.log("Databases found: " + sb.toString() );
    } catch (Exception e) {
      logger.log("Error connecting: " + e.getMessage());
    }

    return null;
  }
}

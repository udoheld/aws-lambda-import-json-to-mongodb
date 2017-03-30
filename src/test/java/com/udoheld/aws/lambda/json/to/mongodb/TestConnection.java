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
import com.mongodb.async.SingleResultCallback;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.junit.Test;

import java.util.GregorianCalendar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Udo Held
 */
public class TestConnection {

  private String mongoDbConnectionUrl = "mongodb://localhost/?connectTimeoutMS=2500&socketTimeoutMS=5000";
  private String mongoDbDatabase = "unitTest";
  private String mongoDbCollection = "testConnection";



  @Test
  public void testConnection(){
    MongoClientURI uri = new MongoClientURI(mongoDbConnectionUrl);

    try (MongoClient mongoClient = new MongoClient(uri);
         MongoCursor cursor = mongoClient.listDatabases().iterator()) {
      assertTrue(cursor.hasNext());
    }
  }

  @Test
  public void testReadWrite(){
    MongoClientURI uri = new MongoClientURI(mongoDbConnectionUrl);
    try (MongoClient mongoClient = new MongoClient(uri)){
      MongoDatabase mongoDbDb = mongoClient.getDatabase(mongoDbDatabase);
      MongoCollection<Document> collection = mongoDbDb.getCollection(mongoDbCollection);

      Document document = new Document("curDate", GregorianCalendar.getInstance().getTime());

      collection.insertOne(document);
      assertNotNull(document.getObjectId("_id"));

      document = new Document("_id",document.getObjectId("_id"));
      FindIterable<Document> documents =  collection.find(document);

      assertTrue(documents.iterator().hasNext());

      DeleteResult result = collection.deleteOne(document);
      assertTrue(result.getDeletedCount() == 1);
    }

  }
}

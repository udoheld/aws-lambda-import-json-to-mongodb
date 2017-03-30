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

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.udoheld.aws.lambda.json.to.mongodb.cfg.Config;
import com.udoheld.aws.lambda.json.to.mongodb.cfg.ConfigurationTest;
import com.udoheld.aws.lambda.json.to.mongodb.cfg.ConfigurationInitializer;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Tests the configuration test.
 * @author Udo Held
 */
public class TestConfigurationTest {
  private Logger logger = Logger.getLogger(this.getClass().getName());

  private String [] envVariables = new String[] {
      ConfigurationInitializer.CFG_DEBUG, ConfigurationInitializer.CFG_LOCALTEST,
      ConfigurationInitializer.CFG_MONGODB_USERNAME, ConfigurationInitializer.CFG_MONGODB_PASSWORD,
      ConfigurationInitializer.CFG_MONGODB_HOSTS, ConfigurationInitializer.CFG_MONGODB_DATABASE,
      ConfigurationInitializer.CFG_MONGODB_OPTIONS
  };

  @Test
  public void testMongoUriBuild(){
    Config config = new Config();
    config.setMongoDbUsername("username");
    config.setMongoDbPassword("pwd");
    config.setMongoDbHosts("host");
    config.setMongoDbDatabase("db");
    config.setMongoDbOptions("opts");

    String uri = ConfigurationInitializer.buildConnectionUri(config);

    assertEquals("mongodb://username:pwd@host/db?opts",uri);
  }

  @Test
  public void testLocalTest() throws IOException {
    ConfigurationTest ct = new ConfigurationTest();

    ct.handleRequest(null, null, getContext(logger));
  }

  public static Context getContext(Logger logger) {
    Context ctx = new Context() {
      @Override
      public String getAwsRequestId() {
        return null;
      }

      @Override
      public String getLogGroupName() {
        return null;
      }

      @Override
      public String getLogStreamName() {
        return null;
      }

      @Override
      public String getFunctionName() {
        return null;
      }

      @Override
      public String getFunctionVersion() {
        return null;
      }

      @Override
      public String getInvokedFunctionArn() {
        return null;
      }

      @Override
      public CognitoIdentity getIdentity() {
        return null;
      }

      @Override
      public ClientContext getClientContext() {
        return null;
      }

      @Override
      public int getRemainingTimeInMillis() {
        return 0;
      }

      @Override
      public int getMemoryLimitInMB() {
        return 0;
      }

      @Override
      public LambdaLogger getLogger() {
        return (msg) -> logger.info(msg);
      }
    };

    return ctx;
  }
}

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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Udo Held
 */
public class TestLambdaHandler {
  private Logger log = Logger.getLogger(this.getClass().getName());

  private String simpleJson = "{\"d\":[{\"bn\":\"urn:dev:mac:784b87a58c3d;temp1\",\"bt\":1485869189.215,\"n\":\"temp\",\"u\":\"Cel\",\"v\":27.9}],\"clientid\":\"edison-1\",\"timestamp\":1485869189339,\"topic\":\"iot/sensordata/prod/json\"}";

  @Test
  public void testLambdahandler() throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(simpleJson.getBytes());

    LambdaHandler lh = new LambdaHandler();
    lh.handleRequest(bis,null,TestConfigurationTest.getContext(log));
  }
}

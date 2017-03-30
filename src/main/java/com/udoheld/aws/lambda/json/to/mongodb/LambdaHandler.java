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
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.udoheld.aws.lambda.json.to.mongodb.cfg.Config;
import com.udoheld.aws.lambda.json.to.mongodb.cfg.ConfigurationInitializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handler for direct JSON input messages.
 * @author Udo Held
 */
public class LambdaHandler implements RequestStreamHandler {
  @Override
  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
      throws IOException {
    Config config = ConfigurationInitializer.initializeConfig(context,false);

    String input = null;
    try {
      input = readInputStream(inputStream);
    } catch (IOException expected) {
    }

    String connectionUri = ConfigurationInitializer.buildConnectionUri(config);

    try (ProcessDataHandler pdh
        = ProcessDataHandler.getProcessDataHandler(connectionUri, config.getMongoDbDatabase())) {
      pdh.processInput(input);
    } catch (Exception e) {
      context.getLogger().log(e.getMessage());
    }

  }

  private String readInputStream(InputStream inputStream) throws IOException {
    StringBuilder sb = new StringBuilder();
    byte [] buf = new byte[128];

    int readBytes = 0;
    while ((readBytes = inputStream.read(buf)) != -1) {
      String bufStr = new String(buf,0,readBytes);
      sb.append(bufStr);
      if (readBytes < buf.length) {
        break;
      }
    }
    return sb.toString();
  }
}

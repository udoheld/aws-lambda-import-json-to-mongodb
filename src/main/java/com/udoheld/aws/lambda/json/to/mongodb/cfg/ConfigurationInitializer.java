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

import com.amazonaws.services.lambda.runtime.Context;
import com.mongodb.MongoClientURI;

/**
 * This class helps initializing the configuration.
 * @author Udo Held
 */
public class ConfigurationInitializer {
  private String unitTestDefaultDb = "unitTest";

  public static final String CFG_DEBUG = "Debug";
  public static final String CFG_DEBUG_INPUT = "Debug_Input";
  public static final String CFG_DISABLE_SNS_REMOVAL = "Disable_SNS_Removal";
  public static final String CFG_LOCALTEST = "localtest";
  public static final String CFG_MONGODB_USERNAME = "MongoDB_Username";
  public static final String CFG_MONGODB_PASSWORD = "MongoDB_Password";
  public static final String CFG_MONGODB_HOSTS = "MongoDB_Hosts";
  public static final String CFG_MONGODB_DATABASE = "MongoDB_Database";
  public static final String CFG_MONGODB_OPTIONS = "MongoDB_Options";
  public static final String CFG_MONGODB_KEEP_CONNECTION = "MongoDB_Keep_Connection";

  private final Config config;
  private final Context context;
  private StringBuilder debugLogBuilder = new StringBuilder();
  private boolean debug = false;
  private final String linSep = System.lineSeparator();

  private ConfigurationInitializer(Context context, boolean forceDebug) {
    config = new Config();
    this.context = context;
    this.debug = forceDebug;
  }

  public static Config initializeConfig(Context context, boolean forceDebug) {
    ConfigurationInitializer cfgInit = new ConfigurationInitializer(context, forceDebug);
    return cfgInit.readConfig();
  }

  private Config readConfig() {

    debugLogBuilder.append("Reading configuration." + linSep);

    if (!debug) {
      debug = readValue(CFG_DEBUG,false);
      config.setDebug(debug);
    }
    config.setLocaltest(readValue(CFG_LOCALTEST, false));
    config.setDebugInput(readValue(CFG_DEBUG_INPUT,false));
    config.setDisableSnsRemoval(readValue(CFG_DISABLE_SNS_REMOVAL, false));

    initMongoDb();

    if (debug) {
      debugLogBuilder.append("Read configuration!" + linSep);
      context.getLogger().log(debugLogBuilder.toString());
    }

    validateConfiguration();

    return config;
  }

  private void initMongoDb() {
    config.setMongoDbUsername(readValue(CFG_MONGODB_USERNAME,""));
    config.setMongoDbPassword(readPasswordValue(CFG_MONGODB_PASSWORD, ""));
    config.setMongoDbHosts(readValue(CFG_MONGODB_HOSTS, config.isLocaltest() ? "localhost" : ""));
    config.setMongoDbOptions(readValue(CFG_MONGODB_OPTIONS,""));
    config.setMongoDbDatabase(readValue(CFG_MONGODB_DATABASE,
        config.isLocaltest() ? unitTestDefaultDb : ""));
    config.setMongoDbKeepConnection(readValue(CFG_MONGODB_KEEP_CONNECTION, false));
  }

  private boolean readValue(String key, boolean defaultValue) {
    boolean value = defaultValue;
    String envValue = readEnvironmentEntry(key);
    if (envValue != null && !envValue.isEmpty()) {
      value = envValue.equalsIgnoreCase("true") || envValue.equals("1");

      if (debug) {
        debugLogBuilder.append("Found value for key: " + key + " value: " + value
            + linSep);
      }
    } else {
      if (debug) {
        debugLogBuilder.append("No valid value found for key: " + key + " using default: "
            + value + linSep);
      }
    }
    return value;
  }

  private String readValue(String key, String defaultValue) {
    String value = defaultValue;
    String envValue = readEnvironmentEntry(key);
    if (envValue != null && !envValue.isEmpty()) {
      value = envValue;
      if (debug) {
        debugLogBuilder.append("Found value for key: " + key + " value: " + value
            + linSep);
      }
    } else {
      if (debug) {
        debugLogBuilder.append("No valid value found for key: " + key + " using default: "
            + value + linSep);
      }
    }
    return value;
  }

  private String readPasswordValue(String key, String defaultValue) {
    String value = defaultValue;
    String envValue = readEnvironmentEntry(key);
    if (envValue != null && !envValue.isEmpty()) {
      value = envValue;
      if (debug) {
        debugLogBuilder.append("Found password value for key: " + key + linSep);
      }
    } else {
      if (debug) {
        debugLogBuilder.append("No valid password value found for key: " + key + linSep);
      }
    }
    return value;
  }

  private String readEnvironmentEntry(String key) {
    if (System.getenv().containsKey(key)) {
      return System.getenv().get(key);
      //Fallback to properties for testing.
    } else if (System.getProperties().containsKey(key)) {
      return System.getProperty(key);
    } else {
      return null;
    }
  }

  private void validateConfiguration() {
    boolean valid = true;

    if (config.isLocaltest()) {
      return;
    }
    StringBuilder valErrors = new StringBuilder();
    valErrors.append("Error during configuration validation.").append(linSep);

    if (config.getMongoDbUsername() == null || config.getMongoDbUsername().isEmpty()) {
      valid = false;
      valErrors.append("ERROR: A valid MongoDB username for the environment variable \""
          + CFG_MONGODB_USERNAME + "\" must be configured in the AWS Management Console."
          + linSep);
    }

    if (config.getMongoDbPassword() == null || config.getMongoDbPassword().isEmpty()) {
      valid = false;
      valErrors.append("ERROR: A valid MongoDB password for the environment variable \""
          + CFG_MONGODB_PASSWORD + "\" must be configured in the AWS Management Console."
          + linSep);
    }

    if (config.getMongoDbHosts() == null || config.getMongoDbHosts().isEmpty()) {
      valid = false;
      valErrors.append("ERROR: A valid MongoDB host value for the environment variable \""
          + CFG_MONGODB_HOSTS + "\" must be configured in the AWS Management Console."
          + linSep);
    }

    if (config.getMongoDbDatabase() == null || config.getMongoDbDatabase().isEmpty()) {
      valid = false;
      valErrors.append("ERROR: A valid MongoDB database name for the environment variable \""
          + CFG_MONGODB_DATABASE + "\" must be configured in the AWS Management Console."
          + linSep);
    }

    if (!valid) {
      context.getLogger().log(valErrors.toString());
      throw new IllegalArgumentException(valErrors.toString());
    }
  }

  /**
   * Builds the MongoDB connection URI out of configuration.
   * @param config Configuration object
   * @return mongoDB Uri
   */
  public static String buildConnectionUri(Config config) {
    String passwordUri = config.getMongoDbPassword().isEmpty() ? ""
        : ":" + config.getMongoDbPassword();
    String userNameUri = config.getMongoDbUsername().isEmpty() ? ""
        : config.getMongoDbUsername() + passwordUri + "@";
    String dbUri = config.getMongoDbDatabase().isEmpty() ? "" : "/" + config.getMongoDbDatabase();
    String optionsUri = config.getMongoDbOptions().isEmpty() ? ""
        : "?" + config.getMongoDbOptions();

    String connectionUri = "mongodb://" + userNameUri + config.getMongoDbHosts()
        + dbUri + optionsUri;
    return connectionUri;
  }
}


# AWS Lambda Import JSON messages to MongoDB
This library can be used for importing messages passed to Lambda using Morphia to MongoDB.
It works well with MongoDB Atlas.

## Configuration
The .jar must be built and uploaded to AWS using the AWS Console.

For building a dependency [`iot-json-input`](https://github.com/udoheld/iot-json-input) is required.

The configuration parameters to MongoDB need to be configured. The samples refer to the MongoDB Atlas configuration.

* `MongoDB_Username` Username for MongoDB e.g. "mongo_admin"
* `MongoDB_Password` Password in clear-text e.g. "password123"
* `MongoDB_Hosts` The host:port combinations. e.g. "test-shard-00-00-abosk.mongodb.net:27017,test-shard-00-01-abosk.mongodb.net:27017,test-shard-00-02-abosk.mongodb.net:27017" If you only have a single host reachable from your Lambda this would work as well.
* `MongoDB_Database` The database you want to connect to e.g. "test"
* `MongoDB_Options` Your connection parameters. Atlas requires `ssl=true`, the `authSource=admin` and  configuration. e.g. `replicaSet=test-shard-0` "authSource=admin&ssl=true&replicaSet=test-shard-0&connectTimeoutMS=10000&maxPoolSize=4"
* `MongoDB_Keep_Connection` This parameter is optional and allows to keep your connection between requests. However, be aware that you will get leaked connections every now and than and that container reuse behaviour isn't guaranteed by AWS.

## AWS Lambda Handlers
* com.udoheld.aws.lambda.json.to.mongodb.cfg.ConfigurationTest lets you test your
connection to MongoDB.
* com.udoheld.aws.lambda.json.to.mongodb.cfg.LambdaHandler runs the
actual import.

## Sample test data
A sample json-file called [`test.json`](src/test/resources/test.json) is included.

The testcases require a running instance of MongoDB at `localhost:27107` without credentials.
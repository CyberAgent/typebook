# typebook [![Build Status](https://travis-ci.org/CyberAgent/typebook.svg?branch=master)](https://travis-ci.org/CyberAgent/typebook)
Registry server for persisting and managing data schemas defined in Avro's schema format.


## Features
* Finagle/Finch based RestAPI implementation hosted on a Twitter HTTP Server
  * store and retrieve schemas
  * evolve schemas (supports semantic versioning, evolution rule definition and enforcement)
* RDBMS storage backend
  * MySQL
  * MariaDB



## Requirements
* MySQL or MariaDB (Tested versions: MySQL 5.7 and MariaDB 10)



## Quickstart
The easiest way to get started is running the service in a Docker container as follows:

```
$ docker-compose -f docker/docker-compose.yml up -d
```

This will start the typebook server in a container with Twitter Server Admin and MySQL server, exposing the service on port `8888`.
Once the service is up, interact with typebook using `curl` or any other HTTP clients.



## Configurations
You can configure the connection to the backend database via the following environment variables.

| name           | description                                 | default                      |
| -------------- | ------------------------------------------- | ---------------------------- |
| MYSQL_SERVERS  | comma-separated database hostname and port  | backend-db:3306              |
| MYSQL_USER     | database username                           | typebook                     |
| MYSQL_PASSWORD | database user password                      |                              |
| MYSQL_DATABASE | database name                               | registry



## Deployment

### Kubernetes
A [helm](https://docs.helm.sh) chart is provided -
please refer to [chart/typebook](charts/typebook) for more details.


### Marathon
An example marathon service spec file is available [here](examples/deploy/marathon/typebook.json).
To deploy on DC/OS, use the following command:

```
$ dcos marathon app add examples/deploy/marathon/typebook.json
```

Please note that a backend database needs to be available and configured via environment variables beforehand.




## API
API documentation for the typebook API are available [here](server).
A Swagger UI is provided as well - see [here](docs) for more details.



## Testing
To execute the test suite, run the following command in the directory containing `build.sbt`.
```
$ sbt test
```


## Contributors
Thanks to all contributors!
- [aberey](https://github.com/aberey)
- [potix2](https://github.com/potix2)
- [tsukaby](https://github.com/tsukaby)
- [saint1991](https://github.com/saint1991)



## License
typebook is Open Source and available under the MIT License.

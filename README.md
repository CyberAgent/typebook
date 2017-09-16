# typebook
Registry server for persisting and managing data schemas defined in Avro's schema format.


## Features
* Finagle/Finch based RestAPI implementation hosted on a Twitter HTTP Server
  * store and retrieve schemas
  * evolve schemas (supports semantic versioning, evolution rule definition and enforcement)
* RDBMS storage backend
  * MySQL
  * MariaDB



## Requirements
* MySQL or MariaDB (Tested Version: MySQL 5.7 and MariaDB 10)



## Quickstart
The easiest way to get started is running the service in a Docker container as follows:

```
$ docker-compose -f docker/docker-compose.yml up -d
```

This will start the typebook server in a container with Twitter Server Admin and MySQL server, exposing the service on port `8888`.
Then you can interact with typebook using `curl` or any other HTTP clients.



## Configurations
You can configure the connection to backend database via following environment variables.

| name           | description                                 | default                      |
| -------------- | ------------------------------------------- | ---------------------------- |
| MYSQL_SERVERS  | comma-separated database hostname and port  | backend-db:3306              |
| MYSQL_USER     | database username                           | typebook                     |
| MYSQL_PASSWORD | database user password                      |                              |
| MYSQL_DATABASE | database name                               | registry     



## Deployment

### Kubernetes
We provide [helm](https://docs.helm.sh) chart. 
Please refer [chart/typebook](chart/typebook) for more details.


### Marathon
An example marathon service spec file is available [here](examples/deploy/marathon/typebook.json).  
For the case of DC/OS, deploying by following command:

```
$ dcos marathon app add examples/deploy/marathon/typebook.json
```

Note that you should prepare backend database beforehand and replace environment variables according to it.




## API
Documents of typebook API is available [here](server).  
We also provide swagger-ui. Please see [here](docs) for more details.



## Testing
To run the test suite, run the following command in the directory containing `build.sbt`.
```
$ sbt test
```


## Contributors
Thanks all for your contributions!
- [aberey](https://github.com/aberey)
- [potix2](https://github.com/potix2)
- [tsukaby](https://github.com/tsukaby)
- [saint1991](https://github.com/saint1991)



## License 
typebook is Open Source and available under the MIT License.

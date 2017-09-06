# Typebook

An Avro Schema Registry.

# What is Typebook?

`Typebook` is a registry service for persisting and managing data schemas defined in Avro's schema format.
It provides a RESTful API and client libraries for access.

# Features

* Finagle/Finch based RestAPI implementation hosted on a Twitter HTTP Server
  * store and retrieve schemas
  * evolve schemas (supports semantic versioning, evolution rule definition and enforcement)
* Client libraries for accesing the API
  * Scala
  * Python
* RDBMS storage backend
  * MySQL
  * MariaDB

# Requirements

* MySQL or MariaDB (Tested Version: MySQL 5.7 and MariaDB 10)

# Quickstart

The easiest way to get started is running the service in a Docker container as follows:

```
$ sbt docker
$ docker-compose -f docker/docker-compose.yml up -d
```

This will start the Typebook server in a container with Twitter Server Admin and MySQL server, exposing the service on port `8888`.


# Build and run tests

To run the test suite, it is necessary to build the docker image beforehand:

```
$ sbt docker
$ sbt test
```

# Deployment

## How to connect to an external database

To connect Typebook to an external database, set the following environment variables before running Typebook:

| name           | description                                 | default                      |
| -------------- | ------------------------------------------- | ---------------------------- |
| MYSQL_SERVERS  | comma-separated database hostname and port  | schema-registry-storage:3306 |
| MYSQL_USER     | database username                           | dev                          |
| MYSQL_PASSWORD | databasa user password                      |                              |
| MYSQL_DATABASE | database name                               | registry                     |


## Kubernetes (Minikube)

First, create a MySQL pod from the official MySQL image. 
Replace `yourpassword` with proper credentials in mysql.yaml.

```
$ kubectl create -f examples/deploy/minikube/mysql.yaml
```

Verify that the pod is up and running:

```
$ kubectl get pod
NAME                        READY     STATUS    RESTARTS   AGE
mysql                       1/1       Running   0          45m
```

Next, deploy the Typebook server and expose its port using a node port.

```
$ kubectl run typebook --image=registry.hub.docker.com/cyberagent/typebook --port=8888 --env="MYSQL_SERVERS=mysql:3306" --env="MYSQL_PASSWORD=yourpassword" --port=8888
$ kubectl expose deployment typebook --name=typebook-nodeport --port=8888 --target-port=8888 --type="NodePort"
```

The service should now be accesible:

```
$ curl $(minikube service typebook-nodeport --url)/health
"OK"
```

## Marathon

Example Marathon config:

```
{
  "id": "typebook.server",
  "container": {
    "type": "DOCKER",
    "docker": {
      "image": "registry.hub.docker.com/cyberagent/typebook",
      "network": "BRIDGE",
      "portMappings": [
        {
          "containerPort": 8888,
          "protocol": "tcp"
        },
        {
          "containerPort": 9990,
          "protocol": "tcp"
        }
      ]
    }
  },
  "instances": 1,
  "cpus": 0.5,
  "mem": 256,
  "env": {
    "MYSQL_SERVERS": "YOUR_MYSQL_SERVER_HOST",
    "MYSQL_USER": "YOUR_MYSQL_USER",
    "MYSQL_PASSWORD": "YOUR_MYSQL_PASSWORD",
    "MYSQL_DATABASE": "registry"
  },
  "healthChecks": [
    {
      "protocol": "HTTP",
      "path": "/health",
      "gracePeriodSeconds": 60,
      "intervalSeconds": 15,
      "timeoutSeconds": 10
    }
  ]
}
```

To deploy using Marathon on DC/OS, save the above configuration as typebook-server.json and run the following command:

```
$ dcos marathon app add typebook-server.json
```

# API

Build and run swagger-ui to see the API documentation.

```
$ sbt docker
$ cd docs
$ docker-compose up
```

Access `http://localhost:8080` in a browser and explore `http://localhost:8080/typebook.yml` as shown below:

![2017-06-19 11 42 32](https://user-images.githubusercontent.com/93571/27267591-78ac258e-54e4-11e7-8537-40517f419497.png)

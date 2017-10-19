# API docs on swagger

This folder contains everything to build and run the Swagger UI for browsing the API documentation.

As a prerequisite, the `cyberagent/typebook:latest` image needs to be available locally - it can be built from the project root (the directory containing build.sbt) as follows:

```
$ sbt docker
```

The Swagger UI can then be started as follows:
```
$ docker-compose up
```

Access `http://$DOCKER_HOST:8080` in a browser and explore `http://$DOCKER_HOST:8080/typebook.yml` as shown below.
`$DOCKER_HOST` is address of your docker host.

![2017-06-19 11 42 32](https://user-images.githubusercontent.com/93571/27267591-78ac258e-54e4-11e7-8537-40517f419497.png)

# API docs on swagger

Build and run swagger-ui to see the API documentation.

If you don't have `cyberagent/typebook:latest` image, you should build the image beforehand.

On the directory containing `build.sbt`, you can build the image as follows:
```
$ sbt docker
```

Then start swagger 
```
$ docker-compose up
```

Access `http://$DOCKER_HOST:8080` in a browser and explore `http://$DOCKER_HOST:8080/typebook.yml` as shown below.
`$DOCKER_HOST` is address of your docker host.

![2017-06-19 11 42 32](https://user-images.githubusercontent.com/93571/27267591-78ac258e-54e4-11e7-8537-40517f419497.png)

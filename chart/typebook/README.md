
# typebook Helm Chart
`typebook` is a registry service for persisting and managing data schemas defined in Avro's schema format.

## TL;DR;
```
$ helm install .
```

## Introduction

This chart bootstraps a typebook deployment on a [Kubernetes](http://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

## Prerequisites
The chart has been tested with:
- Kubernetes: v1.7.5 (GKE and Minikube)
- PV support on the underlying infrastructure (if persistence is desired)

## Install the chart
To install the chart with the release name `my-typebook`:
```
$ helm dependency update
$ helm install --name my-typebook .
```

## Uninstall the chart
To uninstall the release `my-typebook`:
```
$ helm delete my-typebook
```

## Configurations

| Parameter               | Description                                                    | Default                                      |
| ----------------------- | ---------------------------------------------------------------| -------------------------------------------- |
| `image.name`            | typebook image name                                            | `cyberagent/typebook`                        |
| `image.tag`             | typebook image tag                                             | `latest`                                     |
| `image.pullPolicy`      | typebook image pull policy                                     | `InNotPresent`                               |
| `service.type`          | typebook service type                                          | `ClusterIP`                                  |
| `service.internalPort`  | port on which the typebook server listens                      | `8888`                                       |
| `service.externalPort`  | typebook service port                                          | `8888`                                       |
| `service.adminPort`     | port for the Finagle administration dashboard                  | `9090`                                       |
| `replicas`              | number of typebook replicas                                    | `2`                                          |
| `resources`             | typebook resource requests and limits                          | `{requests: {cpu: "100m", memory: "256Mi"}}` |
| `mysql.enabled`         | flag wether to use the mysql chart for the backend database    | `true`                                       |
| `mysql.endpoints`       | list in the form of `IP:PORT` to use external mysql servers    | `nil`                                        |
| `mysql.mysqlUser`       | MySQL user for typebook                                        | `typebook`                                   |
| `mysql.mysqlPassword`   | MySQL password for the user `mysql.mysqlUser`                  | `typebook`                                   |
| `mysql.mysqlDatabase`   | MySQL database name for typebook                               | `registry`                                   |

In addition to the above, some configuration options are available as part of the MySQL chart.
Details are documented [here](https://github.com/kubernetes/charts/tree/master/stable/mysql).
To customize the MySQL chart, set its options using a `mysql.` prefix.

## MySQL
By default, this chart will use a MySQL database deployed via chart dependency.
To use an external MySQL instance, customize values.yml as follows:

```values.yaml
mysql:
    enabled: false
    endpoints:
        - "mysql1:3306"
        - "mysql2:3306"
...
```

```
$ helm install -f values.yaml .
```

### Persistence

When using the MySQL chart, a PersistentVolume is mounted on the MySQL pod. 
The volume is created using dynamic volume provisioning. 
To disable it or change the persistence properties, update the persistence section in values.yaml as follows:

``` values.yaml
mysql:
    persistence:
        enabled: false
...
```


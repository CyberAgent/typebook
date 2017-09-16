
# typebook Helm Chart
`typebook` is a registry service for persisting and managing data schemas defined in Avro's schema format.

## TL;DR;
```
$ helm install .
```

## Introduction

This chart bootstraps a typebook deployment on a [Kubernetes](http://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

## Prerequisites
We check its work under the condition below
- Kubernetes: v1.7.5 (GKE and Minikube)
- PV support on underlying infrastructure (if persistence is required)

## Install the chart
To install the chart with the release name `my-release`:
```
$ helm dependency update
$ helm install --name my-release .
```

## Uninstall the chart
To uninstall the release `my-release`:
```
$ helm delete my-release
```

## Configurations

| Parameter               | Description                           | Default                                                    |
| ----------------------- | ----------------------------------    | ---------------------------------------------------------- |
| `image.name` | typebook image name | `cyberagent/typebook` |
| `image.tag`  | typebook image version | `1.0.0` |
| `image.pullPolicy` | typebook image pull policy | `InNotPresent` |
| `service.type` | typebook service type | `ClusterIP` |
| `service.internalPort` | on which port typebook server listen | `8888` |
| `service.externalPort` | typebook service port | `8888` |
| `service.adminPort` | the port for Finagle administration dashboard | `9090` |
| `replicas` | Number of typebook replicas | `2` |
| `resources` | typebook resource requests and limits | `{requests: {cpu: "100m", memory: "256Mi"}}` |
| `mysql.enabled` | Utilize mysql chart for the backend database | `true` |
| `mysql.endpoints` | List of in the form of `IP:PORT` to use external mysql servers | `nil` |
| `mysql.mysqlUser` | MySQL user for typebook | `typebook` |
| `mysql.mysqlPassword` | MySQL password for the user `mysql.mysqlUser` | `typebook` |
| `mysql.mysqlDatabase` | MySQL database for typebook | `registry` |

For more details of backend MySQL settings, please refer [here](https://github.com/kubernetes/charts/tree/master/stable/mysql)


## Persistence

This chart mounts PersistentVolume on MySQL pod. 
The volume is created using dynamic volume provisioning. 
If you want to disable it or change the persistence properties, update the persistence section of your custom values.yaml file:

``` values.yaml
mysql:
    persistence:
        enabled: false
...
```

## MySQL
By default, this chart will use a MySQL database deployed as a chart dependency.
You can also bring your external MySQL servers.
To do so, set the following in your custom values.yaml file:

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
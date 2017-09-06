
# Index

## Subject
- [`POST /subjects/(string: subject)`](#register-subject)
- [`GET /subjects/(string: subject)[?field={name|description}]`](#read-subject)
- [`GET /subjects`](#read-subjects)
- [`PUT /subjects/(string: subject)`](#update-subject)
- [`DELETE /subjects/(string: subject)`](#delete-subject)

## Schema
- [`POST /subjects/(string: subject)/versions`](#register-schema)
- [`POST /subjects/(string: subject)/schema/lookup`](#lookup-schema)
- [`POST /subjects(string: subject)/schema/lookupAll`](#lookup-all-schemas)
- [`GET /schemas/ids/(int: id)`](#read-schema-by-id)
- [`GET /subjects/(string: subject)/versions/(version)`](#read-schema-by-version)
- [`GET /subjects/(string: subject)/versions`](#read-schema-versions)
- [`POST /compatibility/subjects/(string: subject)/versions/(version)`](#check-compatibility)

## Config
- [`PUT /config/(string: subject)`](#set-config)
- [`PUT /config/(string: subject)/properties/(string: property)`](#set-property)
- [`GET /config/(string: subject)`](#read-config)
- [`GET /config/(string: subject)/properties/(string: property)`](#read-property)
- [`DELETE /config/(string: subject)`](#delete-config)
- [`DELETE /config/(string: subject)/properties/(string: property)`](#delete-property)


# Error Response
```
{
    "error_code": (int: error code),
    "message": (string: error message)
}
```


# APIs


## <a name="register-subject"> `POST /subjects/(string: subject)` </a>
Register a new subject with the posted description.  
Its name might be equal to a topic name in Kafka.

### Request Body (Optional)
`(string: description)`

### Response
| status | response | when |
| :--: | :--: | :--: |
| 201 | (Long: 0L) | succeeded |
| 500 | ErrorResponse | some errors occurred in the backend |



## <a name="read-subject"> `GET /subjects/(string: subject)[?field={name|description}]` </a>
Read the subject by the given name.

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | json or string (details below) | succeeded |
| 404 | ErrorResponse | specified subject is not existing |
| 500 | ErrorResponse | some errors occurred in the backend |

Without specifying `field`, returning full information of the subject
```
{
    "name": (string),
    "description": (string)
}
```

otherwise, returning the value of specified field ("name" or "description")
```
test-topic
```



## <a name="read-subjects"> `GET /subjects` </a>
Read a list of all subjects


### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | json array (details below) | succeeded |
| 500 | ErrorResponse | some errors occurred in the backend |

```
[
    "test-topic",
    "test-topic2",
    "test-topic3"
]
```



## <a name="update-subject"> `PUT /subjects/(string: subject)` </a>
Update the description of the specified subject

### Request Body (Optional)
`(string: description)`

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | (Long: the number of updated rows) | succeeded |
| 500 | ErrorResponse | some errors occurred in the backend |



## <a name="delete-subject"> `DELETE /subjects/(string: subject)` </a>
Delete the specified subject

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | (Long: the number of deleted rows) | succeeded |
| 500 | ErrorResponse | some errors occurred in the backend |



## <a name="register-schema"> `POST /subjects/(string: subject)/versions` </a>
Register a new schema under the specified subject

**NOTE**
Deleting or updating a schema is prohibited in this registry to avoid 
the confusing situation that there exists some different schemas that has the same id, subject and version.
Add it as a new schema instead.

### Request Body
(schema: Avro Schema)

### Response
| status | response | when |
| :--: | :--: | :--: |
| 201 | (int: SchemaId in a json format (details below)) | succeeded |
| 409 | ErrorResponse | the posted schema violates the restriction on schema compatibility |
| 422 | ErrorResponse | the posted schema is not valid |
| 500 | ErrorResponse | some errors occurred in the backend |

Response for successful registration is
```
{
    "id": (int: schema id)
}
```



## <a name="lookup-schema"> `POST /subjects/(string: subject)/schema/lookup` </a>
Check if the posted schema is existing.

### Request Body
(schema: Avro Schema)

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | json (details below) | succeeded |
| 404 | ErrorResponse | the posted schema is not registered |
| 422 | ErrorResponse | the posted schema is invalid as an Avro schema |
| 500 | ErrorResponse | some errors occurred in the backend |

When the posted schema is existing, following is returned.
```
{
    "id": (int),
    "subject": (string),
    "version": (string: semantic version),
    "schema": (json: avro schema)
}
```


## <a name="lookup-all-schemas"> `POST /subjects/(string: subject)/schema/lookupAll`</a>
Lookup the all schemas who have the same definition as the posted one.

### Request Body
(schema: Avro Schema)

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | json (details below) | succeeded |
| 422 | ErrorResponse | the posted schema is invalid as an Avro schema |
| 500 | ErrorResponse | some errors occurred in the backend |

Following response is returned
```
[
    {
        "id": (int),
        "subject": (string),
        "version": (string: semantic version),
        "schema": (json: avro schema)
    },
    {
        "id": (int),
        "subject": (string),
        "version": (string: semantic version),
        "schema": (json: avro schema)
    },...
]
```


## <a name="read-schema-by-id"> `GET /schemas/ids/(int: id)` </a>
Read the schema identified by the given id.

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | json including schema definition (details below) | succeeded |
| 404 | ErrorResponse | schema not found |
| 422 | ErrorResponse | invalid id |
| 500 | ErrorResponse | some errors occurred in the backend |

```
{
    "id": (int: schemaId),
    "subject": (string: subject),
    "version": (string: semantic version),
    "schema": (json: avro schema)
}
```



## <a name="read-schema-by-version"> `GET /subjects/(string: subject)/versions/(version)` </a>
Read a specific version of schema under the specified subject
Version can take a one of the following format, "latest", "v1", "v1.0.0",
In the first case, the latest schema under the specified subject will be returned.
Second case, the given major version of latest schema will be returned.
The last case, the one with specified version is returned.




### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | json including schema definition (details below) | succeeded |
| 404 | ErrorResponse | specified version of schema is not found under the subject
| 422 | ErrorResponse | invalid value is passed as the version |
| 500 | ErrorResponse | some errors occurred in the backend |

```
{
    "id": (int: schemaId),
    "subject": (string: subject),
    "version": (string: semantic version),
    "schema": (json: avro schema)
}
```



## <a name="read-schema-versions"> `GET /subjects/(string: subject)/versions` </a>
Read all versions under the specified subject.
Versions are in the form of semantic version.

Each part of the version is updated when
| part | when |
| :--: | :--: |
| major | forward compatible change, or not compatible change | 
| minor | backward compatible change |
| patch | full compatible change | 

More details, refer

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | json array (details below) | succeeded |
| 500 | ErrorResponse | some errors occurred in the backend |

Example response is following,
```
[
    "v1.0.0", "v1.0.1", "v1.1.0", "v2.0.0"
]
```



## <a name="check-compatibility"> `POST /compatibility/subjects/(string: subject)/versions/(version)` </a>
Check if the posted schema is compatible with the specified version of schema under the subject

### Request Body
Avro schema

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | json (details below) | succeeded |
| 404 | ErrorResponse | the specified version of schema does not exist under the subject
| 422 | ErrorResponse | the posted schema or version is invalid |
| 500 | ErrorResponse | some errors occurred in the backend |

When succeeded, 

```
{
    "is_compatible": (bool)
}
```



## <a name="set-config"> `PUT /config/(string: subject)` </a>
Set a compatibility restriction to the specified subject

### Request Body
```
{
    "compatibility": (string: {"FULL"|"BACKWARD"|"FORWARD"|"NONE"})
}
```

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | (long: the number of affected properties) | succeeded |
| 422 | ErrorResponse | invalid json or invalid compatibility value |
| 500 | ErrorResponse | some errors occurred in the backend |




## <a name="set-property"> `PUT /config/(string: subject)/properties/(string: property)` </a>
Set a value to the specific configuration property under the subject

### Request Body
a value for the property

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | (Long: the number of affected properties) | succeeded |
| 422 | ErrorResponse | an invalid compatibility value |
| 500 | ErrorResponse | some errors occurred in the backend |



## <a name="read-config"> `GET /config/(string: subject)` </a>
Read all configuration properties of the specified subject

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | json (details below) | succeeded |
| 404 | ErrorResponse | try to read non existent subject |
| 500 | ErrorResponse | some errors occurred in the backend |

When succeeded, config is returned in a json format as follows,
```
{
    "compatibility": (string: {"FULL"|"BACKWARD"|"FORWARD"|"NONE"})
}
```



## <a name="read-property"> `GET /config/(string: subject)/properties/(string: property)` </a>
Read a value of the specified property of the subject

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | (string: property value) | succeeded |
| 422 | ErrorResponse | try to read not defined property |
| 500 | ErrorResponse | some errors occurred in the backend |



## <a name="delete-config"> `DELETE /config/(string: subject)` </a>
Delete all configuration properties that has been registered for the specified subject

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | (Long: the number of deleted configs) | succeeded |
| 500 | ErrorResponse | some errors occurred in the backend |



## <a name="delete-property"> `DELETE /config/(string: subject)/properties/(string: property)` </a>
Delete a specific property of the subject

### Response
| status | response | when |
| :--: | :--: | :--: |
| 200 | (Long: the number of deleted configs) | succeeded |
| 500 | ErrorResponse | some errors occurred in the backend |

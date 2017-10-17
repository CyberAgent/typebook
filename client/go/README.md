
# Go Client

typebook client for Go.

## Installation
```
$ go get github.com/cyberagent/typebook/client/go
```

## Getting Started

To import this library, 
```
import typebook "github.com/cyberagent/typebook/client/go"
```

Then instantiate a client as follows,
```
client := typebook.NewClient("localhost:8888")
```

After that, you can interact with typebook using the client!
```
client.CreateSubject("payment", "personal payment data")
schemaId, err := client.RegisterSchema("payment", `
    {
        "namespace": "jp.co.cyberagent.typebook.example",
        "type": "record",
        "name": "Payment",
        "fields": [
            {
                "name": "id",
                "type": "int"
            },
            {
                "name": "name",
                "type": "string"
            },
            {
                "name": "amount",
                "type": "double"
            },
            {
                "name": "time",
                "type": "long"
            }
        ]
    }`)
```

## Configure client behavior
This client is thin wrapper of [gorequest](https://github.com/parnurzeal/gorequest).
Please consult gorequest documentation.

## API
See [GoDoc reference]() for detailed API documentation.

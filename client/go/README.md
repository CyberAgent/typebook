
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
client := typebook.NewClient("localhost:8888", nil)
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


## Configure Client
`NewClient` takes an instance of `http.Transport` as the second argument, 
you can configure it via the argument as follows,
```
client := typebook.NewClient("localhost:8888", &http.Transport{
    MaxIdleConns: 2,
    DisableKeepAlives: true,
})
```
For more details, please refer [net/http](https://golang.org/pkg/net/http/#Transport).

## API
See [GoDoc reference]() for detailed API documentation.

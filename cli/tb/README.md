
# tb

`tb` is CLI to interact with a typebook server.

## Install
```
$ go get github.com/cyberagent/typebook/cli/tb
```

## Configuration
To configure the URL of a typebook server to interact with, 
set it via environment variable or configuration file as follows,

```
$ export TYPEBOOK_URL="$HOST:$PORT"
```
`$HOST` is dns name or IP for the server and `$PORT` is its service port.

Or create a file `.typebook.yml` in your home directory as follows,

```
url: "$HOST:$PORT"
```
If it is not set, tb uses `127.0.0.1:8888` as default.
If both of them exist, environment variable takes precedence.

package cmd

import "github.com/cyberagent/typebook/client/go/model"

const (
	sampleSchemaPath = "../samples/data/schema.avsc"
	schemaDef        = `
{
    "namespace": "com.example",
	"name": "Person",
    "type": "record",
    "fields": [
        {"name": "id", "type": "int"},
        {"name": "first_name", "type": "string"}
    ]
}
`
)

var (
	testSchema = model.Schema{
		Id:         1,
		Subject:    testSubject,
		Version:    model.SemVer{Major: 1, Minor: 0, Patch: 0},
		Definition: schemaDef,
	}
)

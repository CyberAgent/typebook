package _go

import (
	"testing"

	"gopkg.in/h2non/gock.v1"

	"github.com/cyberagent/typebook/client/go/model"
)

const schemaStr = `
{
    "namespace": "com.example",
    "type": "record",
    "name": "Person",
    "fields": [
        {
            "name": "id",
            "type": "int"
        },
        {
            "name": "first_name",
            "type": "string"
        },
        {
            "name": "last_name",
            "type": "string"
        }
    ]
}
`

var schema = model.Schema{
	Id:      1,
	Subject: subject,
	Version: model.SemVer{Major: 1, Minor: 0, Patch: 0},
	Schema:  schemaStr,
}

// POST /subjects/(subject string)/versions
func TestRegisterSchema(t *testing.T) {
	defer gock.Off()

	expect := model.SchemaId{1}
	gock.New(host).
		Post("/subjects/" + subject + "/versions").
		JSON(schemaStr).
		Reply(201).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "8",
		}).
		JSON(expect)

	if actual, err := client.RegisterSchema(subject, schemaStr); err != nil {
		t.Errorf(`RegisterSchema("%s", "%s") should not be an error. But an error was occurred: %v`, subject, schemaStr, err)
	} else if *actual != expect {
		t.Errorf(`RegisterSchema("%s", "%s") = %v, wants %v`, subject, schemaStr, actual, expect)
	}
}

//func TestLookupSchema(t *testing.T) {
//	defer gock.Off()
//
//	expect := model.Schema{
//		Id: 1,
//		Subject: subject,
//		Version: model.SemVer{Major: 1, Minor:0, Patch:0},
//		Schema: `{\"type\":\"record\",\"name\":\"Person\",\"namespace\":\"com.example\",\"fields\":[{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"first_name\",\"type\":\"string\"},{\"name\":\"last_name\",\"type\":\"string\"}]}`,
//	}
//	gock.New(host).
//		Post("/subjects/" + subject + "/schema/lookup").
//		JSON(schemaStr).
//		Reply(200).
//		SetHeaders(map[string]string{
//			"Content-Type":   "application/json",
//			"Content-Length": "274",
//		}).
//		JSON(expect)
//
//	if actual, err := client.LookupSchema(subject, schemaStr); err != nil {
//		t.Errorf(`LookupSchema("%s", "%s") should not be an error. But an error was occurred: %v`, subject, schemaStr, err)
//	} else if *actual != expect {
//		t.Errorf(`LookupSchema("%s", "%s") = %v, wants %v`, subject, schemaStr, actual,expect)
//	}
//}

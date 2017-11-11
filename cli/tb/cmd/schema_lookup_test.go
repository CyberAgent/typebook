package cmd

import (
	"fmt"
	"io/ioutil"
	"testing"

	"gopkg.in/h2non/gock.v1"

	"github.com/cyberagent/typebook/client/go/model"
)

func TestSchemaLookupFromFile(t *testing.T) {
	defer gock.Off()

	fileContent, err := ioutil.ReadFile(sampleSchemaPath)
	if err != nil {
		t.Errorf("failed to open a sample config file: %s", err.Error())
	}

	gock.New(hostForTest).
		Post("/subjects/" + testSubject + "/schema/lookup").
		JSON(fileContent).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "229",
		}).JSON(testSchema)

	args := []string{"schema", "lookup", fmt.Sprintf("@%s", sampleSchemaPath), "--subject", testSubject}
	schemaLookupCmd.Root().SetArgs(args)

	if err := schemaLookupCmd.Execute(); err != nil {
		t.Errorf("schema lookup command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestSchemaLookupAllFromValue(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Post("/subjects/" + testSubject + "/schema/lookupAll").
		JSON(schemaDef).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "461",
		}).JSON([]model.Schema{
		{
			Id:         1,
			Subject:    testSubject,
			Version:    model.SemVer{Major: 1, Minor: 0, Patch: 0},
			Definition: schemaDef,
		},
		{
			Id:         3,
			Subject:    testSubject,
			Version:    model.SemVer{Major: 1, Minor: 0, Patch: 2},
			Definition: schemaDef,
		},
	})

	args := []string{"schema", "lookup", schemaDef, "--subject", testSubject, "--all"}
	schemaLookupCmd.Root().SetArgs(args)

	if err := schemaLookupCmd.Execute(); err != nil {
		t.Errorf("schema lookup command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

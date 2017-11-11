package cmd

import (
	"fmt"
	"io/ioutil"
	"testing"

	"gopkg.in/h2non/gock.v1"

	"github.com/cyberagent/typebook/client/go/model"
)

func TestSchemaCreateFromFile(t *testing.T) {
	defer gock.Off()

	fileContent, err := ioutil.ReadFile(sampleSchemaPath)
	if err != nil {
		t.Errorf("failed to open a sample config file: %s", err.Error())
	}

	gock.New(hostForTest).
		Post("/subjects/" + testSubject + "/versions").
		JSON(fileContent).
		Reply(201).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "8",
		}).
		JSON(model.SchemaId{Id: 1})

	args := []string{"schema", "create", fmt.Sprintf("@%s", sampleSchemaPath), "--subject", testSubject}
	schemaCreateCmd.Root().SetArgs(args)

	if err := schemaCreateCmd.Execute(); err != nil {
		t.Errorf("schema create command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestSchemaCreateFromValue(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Post("/subjects/" + testSubject + "/versions").
		JSON(schemaDef).
		Reply(201).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "8",
		}).
		JSON(model.SchemaId{Id: 1})

	args := []string{"schema", "create", schemaDef, "--subject", testSubject}
	schemaCreateCmd.Root().SetArgs(args)

	if err := schemaCreateCmd.Execute(); err != nil {
		t.Errorf("schema create command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

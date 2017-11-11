package cmd

import (
	"testing"

	"gopkg.in/h2non/gock.v1"
)

func TestSchemaGet(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Get("/subjects/" + testSubject + "/versions/latest").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "229",
		}).JSON(testSchema)

	args := []string{"schema", "get", "--subject", testSubject}
	schemaGetCmd.Root().SetArgs(args)

	if err := schemaGetCmd.Execute(); err != nil {
		t.Errorf("schema get command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestSchemaGetByMajorVer(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Get("/subjects/" + testSubject + "/versions/v1").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "229",
		}).JSON(testSchema)

	args := []string{"schema", "get", "--subject", testSubject, "--version", "v1"}
	schemaGetCmd.Root().SetArgs(args)

	if err := schemaGetCmd.Execute(); err != nil {
		t.Errorf("schema get command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestSchemaGetBySemVer(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Get("/subjects/" + testSubject + "/versions/v1.2.1").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "229",
		}).JSON(testSchema)

	args := []string{"schema", "get", "--subject", testSubject, "--version", "v1.2.1"}
	schemaGetCmd.Root().SetArgs(args)

	if err := schemaGetCmd.Execute(); err != nil {
		t.Errorf("schema get command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestSchemaGetById(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Get("/schemas/ids/1").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "229",
		}).JSON(testSchema)

	args := []string{"schema", "get", "--id", "1"}
	schemaGetCmd.Root().SetArgs(args)

	if err := schemaGetCmd.Execute(); err != nil {
		t.Errorf("schema get command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

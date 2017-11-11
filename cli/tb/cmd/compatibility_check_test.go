package cmd

import (
	"fmt"
	"io/ioutil"
	"testing"

	"gopkg.in/h2non/gock.v1"

	"github.com/cyberagent/typebook/client/go/model"
)

func TestCompatibilityCheckFileWithLatest(t *testing.T) {
	defer gock.Off()

	fileContent, err := ioutil.ReadFile(sampleSchemaPath)
	if err != nil {
		t.Errorf("failed to open a sample config file: %s", err.Error())
	}

	gock.New(hostForTest).
		Post("/compatibility/subjects/" + testSubject + "/versions/latest").
		JSON(fileContent).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "135",
		}).
		JSON(model.Compatibility{IsCompatible: true})

	args := []string{"compatibility", "check", fmt.Sprintf("@%s", sampleSchemaPath), "--subject", testSubject}
	compatibilityCheckCmd.Root().SetArgs(args)

	if err := compatibilityCheckCmd.Execute(); err != nil {
		t.Errorf("compatibility check command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestCompatibilityCheckValueWithMajorVer(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Post("/compatibility/subjects/" + testSubject + "/versions/v1").
		BodyString(schemaDef).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "23",
		}).
		JSON(model.Compatibility{IsCompatible: false})

	args := []string{"compatibility", "check", schemaDef, "--subject", testSubject, "--version", "v1"}
	compatibilityCheckCmd.Root().SetArgs(args)

	if err := compatibilityCheckCmd.Execute(); err != nil {
		t.Errorf("compatibility check command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestCompatibilityCheckValueWithSemVer(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Post("/compatibility/subjects/" + testSubject + "/versions/v1.2.1").
		BodyString(schemaDef).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "23",
		}).
		JSON(model.Compatibility{IsCompatible: false})

	args := []string{"compatibility", "check", schemaDef, "--subject", testSubject, "--version", "v1.2.1"}
	compatibilityCheckCmd.Root().SetArgs(args)

	if err := compatibilityCheckCmd.Execute(); err != nil {
		t.Errorf("compatibility check command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

package cmd

import (
	"fmt"
	"io/ioutil"
	"testing"

	"gopkg.in/h2non/gock.v1"
)

const sampleConfigPath = "../samples/data/config.json"

func TestConfigSetFromFile(t *testing.T) {
	defer gock.Off()

	fileContent, err := ioutil.ReadFile(sampleConfigPath)
	if err != nil {
		t.Errorf("failed to open a sample config file: %s", err.Error())
	}

	gock.New(hostForTest).
		Put("/config/" + testSubject).
		JSON(fileContent).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	args := []string{"config", "set", fmt.Sprintf("@%s", sampleConfigPath), "--subject", testSubject}
	configSetCmd.Root().SetArgs(args)

	if err := configSetCmd.Execute(); err != nil {
		t.Errorf("config set command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestConfigSetFromValue(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Put("/config/" + testSubject).
		JSON(map[string]string{"compatibility": "BACKWARD"}).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	args := []string{"config", "set", `{"compatibility": "BACKWARD"}`, "--subject", testSubject}
	configSetCmd.Root().SetArgs(args)

	if err := configSetCmd.Execute(); err != nil {
		t.Errorf("config set command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestPropertySet(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Put("/config/" + testSubject + "/properties/compatibility").
		BodyString("FORWARD").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	args := []string{"config", "set", "compatibility", "FORWARD", "--subject", testSubject}
	configSetCmd.Root().SetArgs(args)

	if err := configSetCmd.Execute(); err != nil {
		t.Errorf("config set property command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

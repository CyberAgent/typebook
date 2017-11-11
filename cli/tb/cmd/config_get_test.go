package cmd

import (
	"testing"

	"gopkg.in/h2non/gock.v1"

	"github.com/cyberagent/typebook/client/go/model"
)

var testConfig = model.Config{Compatibility: "BACKWARD"}

func TestConfigGet(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Get("/config/" + testSubject).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "28",
		}).
		JSON(testConfig)

	args := []string{"config", "get", "--subject", testSubject}
	configGetCmd.Root().SetArgs(args)

	if err := configGetCmd.Execute(); err != nil {
		t.Errorf("config get command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestPropertyGet(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Get("/config/" + testSubject + "/properties/compatibility").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "8",
		}).
		BodyString("BACKWARD")

	args := []string{"config", "get", "compatibility", "--subject", testSubject}
	configGetCmd.Root().SetArgs(args)

	if err := configGetCmd.Execute(); err != nil {
		t.Errorf("config get property command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

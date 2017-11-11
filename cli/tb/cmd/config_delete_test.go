package cmd

import (
	"testing"

	"gopkg.in/h2non/gock.v1"
)

func TestConfigDelete(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Delete("/config/" + testSubject).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	args := []string{"config", "delete", "--subject", testSubject}
	configDeleteCmd.Root().SetArgs(args)

	if err := configDeleteCmd.Execute(); err != nil {
		t.Errorf("config delete command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

func TestPropertyDelete(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Delete("/config/" + testSubject + "/properties/compatibility").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	args := []string{"config", "delete", "compatibility", "--subject", testSubject}
	configDeleteCmd.Root().SetArgs(args)

	if err := configDeleteCmd.Execute(); err != nil {
		t.Errorf("config delete property command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

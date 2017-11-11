package cmd

import (
	"testing"

	"gopkg.in/h2non/gock.v1"
)

func TestSubjectGet(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Get("/subjects/" + testSubject).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "52",
		}).
		JSON(map[string]string{"name": testSubject, "description": testDescription})

	args := []string{"subject", "get", testSubject}
	subjectGetCmd.Root().SetArgs(args)

	if err := subjectGetCmd.Execute(); err != nil {
		t.Errorf("subject get command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

package cmd

import (
	"testing"

	"gopkg.in/h2non/gock.v1"
)

func TestSubjectList(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Get("/subjects").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "34",
		}).
		JSON([]string{"another-subject", "test-subject"})
	gock.New(hostForTest).
		Get("/subjects/another-subject").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "64",
		}).
		JSON(map[string]string{"name": "another-subject", "description": "This is another subject"})
	gock.New(hostForTest).
		Get("/subjects/test-subject").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "52",
		}).
		JSON(map[string]string{"name": "test-subject", "description": testDescription})

	args := []string{"subject", "list"}
	subjectListCmd.Root().SetArgs(args)

	if err := subjectListCmd.Execute(); err != nil {
		t.Errorf("subject list command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

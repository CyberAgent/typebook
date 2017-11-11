package cmd

import (
	"testing"

	"gopkg.in/h2non/gock.v1"
)

func TestSubjectCreate(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Post("/subjects/" + testSubject).
		BodyString(testDescription).
		Reply(201).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("0")

	args := []string{"subject", "create", testSubject, "--description", testDescription}
	subjectCreateCmd.Root().SetArgs(args)

	if err := subjectCreateCmd.Execute(); err != nil {
		t.Errorf("subject create command is expected to be success with args %v but an error was occured %v", args, err)
	}

}

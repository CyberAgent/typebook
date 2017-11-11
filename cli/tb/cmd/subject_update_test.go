package cmd

import (
	"testing"

	"gopkg.in/h2non/gock.v1"
)

func TestSubjectUpdate(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Put("/subjects/" + testSubject).
		BodyString(testDescription).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	args := []string{"subject", "update", testSubject, "--description", testDescription}
	subjectUpdateCmd.Root().SetArgs(args)

	if err := subjectUpdateCmd.Execute(); err != nil {
		t.Errorf("subject update command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

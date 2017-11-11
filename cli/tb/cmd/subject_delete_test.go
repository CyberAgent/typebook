package cmd

import (
	"testing"

	"gopkg.in/h2non/gock.v1"
)

func TestSubjectDelete(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Delete("/subjects/" + testSubject).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	args := []string{"subject", "delete", testSubject}
	subjectDeleteCmd.Root().SetArgs(args)

	if err := subjectDeleteCmd.Execute(); err != nil {
		t.Errorf("subject delete command is expected to be success with args %v but an error was occured %v", args, err)
	}

}

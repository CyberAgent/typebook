package cmd

import (
	"testing"

	"gopkg.in/h2non/gock.v1"
)

func TestListSchemaVersions(t *testing.T) {
	defer gock.Off()

	gock.New(hostForTest).
		Get("/subjects/" + testSubject + "/versions").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "28",
		}).JSON(`["v1.0.0", "v1.0.1", "v1.0.2"]`)

	args := []string{"schema", "list", "versions", "--subject", testSubject}
	schemaListVersionsCmd.Root().SetArgs(args)

	if err := schemaListVersionsCmd.Execute(); err != nil {
		t.Errorf("schema list versions command is expected to be success with args %v but an error was occured %v", args, err)
	}
}

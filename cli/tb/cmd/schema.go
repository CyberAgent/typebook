// The MIT License (MIT)
//
// Copyright © 2017 CyberAgent, Inc.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package cmd

import (
	"encoding/json"
	"fmt"

	"github.com/gosuri/uitable"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"

	"github.com/cyberagent/typebook/client/go/model"
)

var schemaCmd = &cobra.Command{
	Use:   "schema",
	Short: "manage and look up schemas under a subject",
	Long:  "Manage and look up schemas under a subject.",
	PersistentPreRun: func(cmd *cobra.Command, args []string) {
		viper.BindPFlag("subject", cmd.Flags().Lookup("subject"))
	},
}

func init() {
	RootCmd.AddCommand(schemaCmd)

	schemaCmd.PersistentFlags().String("subject", "", "name of subject")
}

func showSchemaMetas(schemas ...model.Schema) {
	table := uitable.New()
	table.AddRow("ID", "SUBJECT", "VERSION")
	for _, schema := range schemas {
		table.AddRow(schema.Id, schema.Subject, schema.Version.String())
	}
	fmt.Println(table)
	fmt.Println()
}

func showSchemaVersions(versions ...model.SemVer) {
	table := uitable.New()
	table.AddRow("VERSION")
	for _, version := range versions {
		table.AddRow(version.String())
	}
	fmt.Println(table)
	fmt.Println()
}

func getPrettySchemaDef(schema *model.Schema) (string, error) {
	intermediate := make(map[string]interface{})
	if err := json.Unmarshal([]byte(schema.Definition), &intermediate); err != nil {
		return "", err
	}

	js, err := prettyJSON(intermediate, 2)
	if err != nil {
		return "", err
	}
	return string(js), nil
}

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
	"fmt"
	"strconv"

	"github.com/spf13/cobra"
	"github.com/spf13/viper"

	typebook "github.com/cyberagent/typebook/client/go"
	"github.com/cyberagent/typebook/client/go/model"
)

var schemaGetCmd = &cobra.Command{
	Use:   "get",
	Short: "get a schema",
	Long: `Retrieve a schema based on an ID or the pair of a subject and a version.
Either id or subject should be provided. If both are specified, id takes a precedence.
version is optional. If omitted, the latest schema under the subject is retrieved.
Available form of version is semantic version (e.g. v1.0.0) or major version (e.g. v1).`,
	PreRun: func(cmd *cobra.Command, args []string) {
		viper.BindPFlag("id", cmd.Flags().Lookup("id"))
		viper.BindPFlag("version", cmd.Flags().Lookup("version"))
	},
	Run: func(cmd *cobra.Command, args []string) {

		id := viper.GetInt64("id")
		subject := viper.GetString("subject")
		version := viper.GetString("version")

		client := typebook.NewClient(viper.GetString("url"))
		if id != -1 {
			showSchemaDef(func() (*model.Schema, error) {
				return client.GetSchemaById(id)
			})
		} else if subject != "" && version == "" {
			showSchemaDef(func() (*model.Schema, error) {
				return client.GetLatestSchema(subject)
			})
		} else if subject != "" && model.IsMajorVer(version) {
			majorVer, _ := strconv.Atoi(version[1:])
			showSchemaDef(func() (*model.Schema, error) {
				return client.GetSchemaByMajorVersion(subject, majorVer)
			})
		} else if subject != "" && model.IsSemVer(version) {
			semver, _ := model.NewSemVer(version)
			showSchemaDef(func() (*model.Schema, error) {
				return client.GetSchemaBySemVer(subject, *semver)
			})
		} else if version != "" {
			exitWithUsage(cmd, fmt.Errorf("invalid format version `%s`. Valid forms are major version (e.g. v1) or semantic version (e.g. v1.0.0)", version))
		} else {
			exitWithUsage(cmd, fmt.Errorf("id or subject should be specified"))
		}
	},
}

func init() {
	schemaCmd.AddCommand(schemaGetCmd)

	schemaGetCmd.Flags().Int("id", -1, "ID of schema")

	schemaGetCmd.Flags().String("version", "", "version of schema")
}

func showSchemaDef(f func() (*model.Schema, error)) {
	schema, err := f()
	if err != nil {
		exitWithError(err)
	}
	if js, err := getPrettySchemaDef(schema); err != nil {
		exitWithError(fmt.Errorf("failed to decode schema: %v", err))
	} else {
		fmt.Println(js)
	}
}

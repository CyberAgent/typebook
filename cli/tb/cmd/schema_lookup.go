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

	"github.com/spf13/cobra"
	"github.com/spf13/viper"

	typebook "github.com/cyberagent/typebook/client/go"
)

var schemaLookupCmd = &cobra.Command{
	Use:   "lookup (@$path | $definition)",
	Short: "lookup schema ID and version",
	Long: `Look up ID and version of schemas based on the posted definition.
When --all flag is provided, all schemas match the definition are retrieved.
Otherwise, the latest one is picked.
This command takes one argument that represents a path to a schema file or definition itself.
A path should begin with @.`,
	Args: cobra.ExactArgs(1),
	PreRun: func(cmd *cobra.Command, args []string) {
		viper.BindPFlag("all", cmd.Flags().Lookup("all"))
	},
	Run: func(cmd *cobra.Command, args []string) {

		subject := viper.GetString("subject")
		if subject == "" {
			exitWithUsage(cmd, fmt.Errorf("subject is not specified"))
		}

		all := viper.GetBool("all")
		client := typebook.NewClient(viper.GetString("url"))

		content, err := valueOrFromPath(args[0])
		if err != nil {
			exitWithError(err)
		}

		if all {
			if metas, err := client.LookupAllSchemas(subject, string(content)); err != nil {
				exitWithError(err)
			} else {
				showSchemaMetas(metas...)
			}
		} else {
			if meta, err := client.LookupSchema(subject, string(content)); err != nil {
				exitWithError(err)
			} else {
				showSchemaMetas(*meta)
			}
		}
	},
}

func init() {
	schemaCmd.AddCommand(schemaLookupCmd)

	schemaLookupCmd.Flags().Bool("all", false, "show all schemas that conforms to posted one")
}

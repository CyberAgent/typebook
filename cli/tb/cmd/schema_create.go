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
)

var schemaCreateCmd = &cobra.Command{
	Use:   "create (@$path | $definition)",
	Short: "create a new schema",
	Long: `Create a new schema under the specified subject.
Unique ID and semantic version are assigned to the schema taking compatibility with existing schemas into account.
This command takes one argument that represents a path to a schema file or definition itself.
A path should begin with @.`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {

		subject := viper.GetString("subject")
		if subject == "" {
			exitWithUsage(cmd, fmt.Errorf("subject is not specified"))
		}

		client := newClient()
		if content, err := valueOrFromPath(args[0]); err != nil {
			exitWithError(err)
		} else {
			if id, err := client.RegisterSchema(subject, string(content)); err != nil {
				exitWithError(err)
			} else {
				fmt.Printf("Schema is registered successfully with ID `%d`", id.Id)
				fmt.Println()
			}
		}
	},
}

func init() {
	schemaCmd.AddCommand(schemaCreateCmd)
}

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

var subjectUpdateCmd = &cobra.Command{
	Use:   "update $subject",
	Short: "update subject description",
	Long:  "Update description of a specified subject.",
	Args:  cobra.ExactArgs(1),
	PreRun: func(cmd *cobra.Command, args []string) {
		viper.BindPFlag(descriptionKey, cmd.Flags().Lookup(descriptionKey))
	},
	Run: func(cmd *cobra.Command, args []string) {

		name := args[0]
		description := viper.GetString("description")

		client := newClient()
		if updatedRows, err := client.UpdateDescription(name, description); err != nil {
			exitWithError(err)
		} else if updatedRows == 1 {
			fmt.Printf("the description for Subject `%s` is updated.\n", name)
		}
	},
}

func init() {
	subjectCmd.AddCommand(subjectUpdateCmd)

	subjectUpdateCmd.Flags().StringP(descriptionKey, "d", "", "description for a subject")
}

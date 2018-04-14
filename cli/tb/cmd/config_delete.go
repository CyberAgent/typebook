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

	"github.com/cyberagent/typebook/client/go/model"
)

var configDeleteCmd = &cobra.Command{
	Use:   "delete [$property]",
	Short: "delete a config from a subject",
	Long: fmt.Sprintf(`Delete a config from a subject.
This command takes one optional argument "property".
If specified, only specific property of config is deleted.
Available properties are following for now,

%s

The deleted property will be back to the default.`, propertyDescriptions()),
	ValidArgs: model.ListProperties(),
	PreRun: func(cmd *cobra.Command, args []string) {
		viper.BindPFlag("subject", cmd.Flags().Lookup("subject"))
	},
	Run: func(cmd *cobra.Command, args []string) {

		subject := viper.GetString("subject")
		if subject == "" {
			exitWithUsage(cmd, fmt.Errorf("subject is not specified"))
		}

		client := newClient()
		if len(args) == 0 { // config delete
			if _, err := client.DeleteConfig(subject); err != nil {
				exitWithError(err)
			}
			fmt.Printf("Config of the subject `%s` is deleted successfully.\n", subject)
		} else if len(args) == 1 { // config delete $property
			property := args[0]
			if _, err := client.DeleteProperty(subject, property); err != nil {
				exitWithError(err)
			}
			fmt.Printf("Config `%s` of the subject `%s` is deleted successfully. \n", property, subject)
		} else {
			exitWithError(fmt.Errorf("too much arguments"))
		}
	},
}

func init() {
	configCmd.AddCommand(configDeleteCmd)

	configDeleteCmd.Flags().String("subject", "", "name of subject (required)")
}

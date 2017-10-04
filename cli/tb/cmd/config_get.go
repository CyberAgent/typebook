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
	"github.com/cyberagent/typebook/client/go/model"
)

var configGetCmd = &cobra.Command{
	Use:   "get [$property]",
	Short: "get a config of a subject",
	Long: fmt.Sprintf(`Retrieve and show a config of a specified subject.
This command takes one optional argument "property".
If specified, the value of specific property of config is shown.
Available properties are following for now,

%s`, propertyDescriptions()),
	ValidArgs: model.ListProperties(),
	PreRun: func(cmd *cobra.Command, args []string) {
		viper.BindPFlag("subject", cmd.Flags().Lookup("subject"))
	},
	Run: func(cmd *cobra.Command, args []string) {

		subject := viper.GetString("subject")
		if subject == "" {
			exitWithUsage(cmd, fmt.Errorf("subject is not specified"))
		}

		client := typebook.NewClient(viper.GetString("url"))
		if len(args) == 0 { // config get
			conf, err := client.GetConfig(subject)
			if err != nil {
				exitWithError(err)
			}
			showConfig(conf)
		} else if len(args) == 1 { // config get $property
			property := args[0]
			value, err := client.GetProperty(subject, property)
			if err != nil {
				exitWithError(err)
			}
			fmt.Println(value)
			fmt.Println()
		} else {
			exitWithError(fmt.Errorf("too much arguments"))
		}
	},
}

func init() {
	configCmd.AddCommand(configGetCmd)

	configGetCmd.Flags().String("subject", "", "name of subject (required)")
}

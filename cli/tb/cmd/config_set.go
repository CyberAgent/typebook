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

	"github.com/spf13/cobra"
	"github.com/spf13/viper"

	"github.com/cyberagent/typebook/client/go/model"
)

var configSetCmd = &cobra.Command{
	Use:   "set ( (@$path | $definition) | ($property $value) )]",
	Short: "set a config to a subject",
	Long: fmt.Sprintf(`Set or update a config as the posted one.

The command may take up to 2 arguments.

When only one argument is provided, the first argument should be a path to json file or definition itself
and overwrite the whole config with the new one. A path should begin with @.

When two arguments are supplied, the first argument should be a property name and the second is its value.
Available properties are following for now,

%s`, propertyDescriptions()),
	ValidArgs: model.ListProperties(),
	Args:      cobra.RangeArgs(1, 2),
	PreRun: func(cmd *cobra.Command, args []string) {
		viper.BindPFlag("subject", cmd.Flags().Lookup("subject"))
	},
	Run: func(cmd *cobra.Command, args []string) {

		subject := viper.GetString("subject")
		if subject == "" {
			exitWithUsage(cmd, fmt.Errorf("subject is not specified"))
		}

		client := newClient()
		if len(args) == 1 { // set whole config
			content, err := valueOrFromPath(args[0])
			if err != nil {
				exitWithError(err)
			}
			conf := new(model.Config)
			if err := json.Unmarshal(content, conf); err != nil {
				exitWithError(err)
			}
			if _, err := client.SetConfig(subject, *conf); err != nil {
				exitWithError(err)
			} else {
				fmt.Printf("Config is set to the subject `%s` as follows,\n", subject)
				showConfig(conf)
			}
		} else if len(args) == 2 { // set the value to specific property
			if _, err := client.SetProperty(subject, args[0], args[1]); err != nil {
				exitWithError(err)
			} else {
				fmt.Printf("Property `%s` is set to the subject `%s` with value `%s`\n", args[0], subject, args[1])
			}
		} else {
			exitWithUsage(cmd, fmt.Errorf("too much arguments"))
		}
	},
}

func init() {
	configCmd.AddCommand(configSetCmd)

	configSetCmd.Flags().String("subject", "", "target subject to set config (required)")
}

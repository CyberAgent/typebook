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

var compatibilityCheckCmd = &cobra.Command{
	Use:   "check (@$path | $definition)",
	Short: "check if the posted schema is compatible with specified one",
	Long: `Check if the posted schema is compatible with the one specified by flags.
This takes a path to schema file or definition as the first argument.
A path should begin with @.
If version is omitted it compares with the latest schema under the subject.
Possible values for version is what represents major version (e.g. v1) or semantic version (e.g. v1.0.0)`,
	Args: cobra.ExactArgs(1),
	PreRun: func(cmd *cobra.Command, args []string) {
		viper.BindPFlag("subject", cmd.Flags().Lookup("subject"))
		viper.BindPFlag("version", cmd.Flags().Lookup("version"))
	},
	Run: func(cmd *cobra.Command, args []string) {

		subject := viper.GetString("subject")
		version := viper.GetString("version")
		if subject == "" {
			exitWithError(fmt.Errorf("subject is not specified"))
		}

		content, err := valueOrFromPath(args[0])
		if err != nil {
			exitWithError(err)
		}

		client := typebook.NewClient(viper.GetString("url"))
		if version == "" {
			showIsCompatible(func() (*model.Compatibility, *model.Error) {
				return client.CheckCompatibilityWithLatest(subject, string(content))
			})
		} else if model.IsMajorVer(version) {
			majorVer, _ := strconv.Atoi(version[1:])
			showIsCompatible(func() (*model.Compatibility, *model.Error) {
				return client.CheckCompatibilityWithMajorVersion(subject, majorVer, string(content))
			})
		} else if model.IsSemVer(version) {
			semver, _ := model.NewSemVer(version)
			showIsCompatible(func() (*model.Compatibility, *model.Error) {
				return client.CheckCompatibilityWithSemVer(subject, *semver, string(content))
			})
		} else {
			exitWithUsage(cmd, fmt.Errorf("invalid format version `%s` Valid forms are major version (e.g. v1) or semantic version (e.g. v1.0.0)", version))
		}
	},
}

func init() {
	compatibilityCmd.AddCommand(compatibilityCheckCmd)

	compatibilityCheckCmd.Flags().String("subject", "", "name of subject (required)")
	compatibilityCheckCmd.Flags().String("version", "", "version of comparison (optional).")
}

func showIsCompatible(f func() (*model.Compatibility, *model.Error)) {
	if compatibility, err := f(); err != nil {
		exitWithError(err)
	} else {
		if compatibility.IsCompatible {
			fmt.Println("Compatible")
		} else {
			fmt.Println("Incompatible")
		}
		fmt.Println()
	}
}

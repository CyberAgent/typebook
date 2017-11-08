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

	"github.com/gosuri/uitable"
	"github.com/spf13/cobra"

	"github.com/cyberagent/typebook/client/go/model"
)

var configCmd = &cobra.Command{
	Use:   "config",
	Short: "manage the config of a subject",
	Long:  "Manage the config of a subject.",
}

func init() {
	RootCmd.AddCommand(configCmd)
}

func showConfig(conf *model.Config) {
	table := uitable.New()
	table.AddRow("PROPERTY", "VALUE")
	table.AddRow(model.CompatibilityProp, conf.Compatibility)
	fmt.Println(table)
	fmt.Println()
}

func propertyDescriptions() string {
	table := uitable.New()
	table.AddRow("PROPERTY", "DESCRIPTION")
	for prop, description := range model.Properties {
		table.AddRow(prop, description)
	}
	return table.String()
}

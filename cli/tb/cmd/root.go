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
	"io/ioutil"
	"log"
	"os"
	"strings"

	homedir "github.com/mitchellh/go-homedir"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
)

// RootCmd represents the base command when called without any subcommands
var RootCmd = &cobra.Command{
	Use:   "tb",
	Short: "typebook client CLI",
	Long:  "tb is a typebook CLI to interact with a typebook server.",
}

// Execute adds all child commands to the root command and sets flags appropriately.
// This is called by main.main(). It only needs to happen once to the rootCmd.
func Execute() {
	if err := RootCmd.Execute(); err != nil {
		fmt.Println(err)
		os.Exit(1)
	}
}

func init() {
	cobra.OnInitialize(initConfig, initFlags)
}

// initConfig reads in config file and ENV variables if set.
func initConfig() {

	home, err := homedir.Dir()
	if err != nil {
		fmt.Println(err)
		os.Exit(1)
	}

	// Search config in home directory with name ".typebook" (without extension).
	viper.AddConfigPath(home)
	viper.SetConfigName(".typebook")

	viper.SetEnvPrefix("TYPEBOOK")
	viper.AutomaticEnv() // read in environment variables that match

	// If a config file is found, read it in.
	viper.ReadInConfig()
}

func initFlags() {
	if RootCmd.PersistentFlags().Lookup("url") == nil {
		RootCmd.PersistentFlags().String("url", "127.0.0.1:8888", "URL of a typebook server")
		viper.BindPFlag("url", RootCmd.PersistentFlags().Lookup("url"))
	}
}

// string begin with `@` is considered as a path
func isPath(arg string) bool {
	return arg[0] == '@'
}

// rvalueOrFromPath returns given value itself.
// If given value is a path, read file content and returns it.
func valueOrFromPath(value string) ([]byte, error) {
	if isPath(value) {
		return ioutil.ReadFile(value[1:])
	} else {
		return []byte(value), nil
	}
}

func exitWithError(err error) {
	log.Fatalln(err.Error())
}

func exitWithUsage(cmd *cobra.Command, err error) {
	fmt.Println(err.Error())
	cmd.Usage()
	os.Exit(1)
}

func prettyJSON(v interface{}, indent int) ([]byte, error) {
	return json.MarshalIndent(v, "", strings.Repeat(" ", indent))
}

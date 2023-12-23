/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>

*/
package cmd

import (
	"fmt"
	"io/ioutil"

	"github.com/fatih/color"
	"github.com/spf13/cobra"
)

// infoCmd represents the info command
var infoCmd = &cobra.Command{
	Use:   "info",
	Short: "will show all the input values",
	Long:  `.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("Present values: \n ")
		info()

	},
}

func init() {
	rootCmd.AddCommand(infoCmd)

	// Here you will define your flags and configuration settings.

	// Cobra supports Persistent Flags which will work for this command
	// and all subcommands, e.g.:
	// infoCmd.PersistentFlags().String("foo", "", "A help for foo")

	// Cobra supports local flags which will only run when this command
	// is called directly, e.g.:
	// infoCmd.Flags().BoolP("toggle", "t", false, "Help message for toggle")
}

func info() {
	// m := []string{"config.txt", "config1.txt", "config2.txt"}
	// for n, _ := range m {
	// 	value, _ := os.ReadFile(userhome + "/.medbuild/" + m)
	config, _ := ioutil.ReadFile(userhome + "/.medbuild/" + "config.txt")
	fmt.Print("Input Directory: ")
	color.Cyan(string(config))
	// config1, _ := os.ReadFile(userhome + "/.medbuild/" + "config1.txt")
	// fmt.Print("-Dportable: ", yellow(string(config1)))
	config2, _ := ioutil.ReadFile(userhome + "/.medbuild/" + "config2.txt")
	fmt.Print("\nJAVA_PATH: ")
	color.Cyan(string(config2))
	// config3, _ := ioutil.ReadFile(userhome + "/.medbuild/" + "config3.txt")
	// fmt.Print("\nAutorun: ")
	// color.Cyan(string(config3))

}

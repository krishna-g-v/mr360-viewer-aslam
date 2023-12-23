/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>

*/
package cmd

import (
	"fmt"

	"github.com/spf13/cobra"
)

// autobuildCmd represents the autobuild command
var autobuildCmd = &cobra.Command{
	Use:   "autobuild",
	Short: "A one-stop to build medreport and create MSI",
	Long: `will build medreport
				-mvn clean install -Dchangelist=
				-mvn clean package -Dchangelist= -Dportable=true -P compressXZ -f weasis-distributions 
		   will create MSI.`,
	Run: func(cmd *cobra.Command, args []string) {
		status, _ := cmd.Flags().GetString("status")

		if status != "" {
			// status := "silent"
			fmt.Println("\n\nRunning autobuild in silent mode")
			cleanDistribution(status)
			actual_code(status)
			msi_installer()
			// fmt.Println("not orinted")
		} else {
			// fmt.Println("orinted")
			fmt.Println("\n\nautobuild called")
			cleanDistribution("something")
			actual_code("something")
			msi_installer()
		}

	},
}

func init() {

	rootCmd.AddCommand(autobuildCmd)
	autobuildCmd.PersistentFlags().String("status", "", "will run silently, only success message will be shown, if failure occurs, the whole log will be printed")

	// Here you will define your flags and configuration settings.

	// Cobra supports Persistent Flags which will work for this command
	// and all subcommands, e.g.:
	// autobuildCmd.PersistentFlags().String("foo", "", "A help for foo")

	// Cobra supports local flags which will only run when this command
	// is called directly, e.g.:
	// autobuildCmd.Flags().BoolP("toggle", "t", false, "Help message for toggle")
}

package cmd

import (
	"regexp"
	"strings"

	"github.com/fatih/color"
	"github.com/spf13/cobra"
)

var (
	rootCmd = &cobra.Command{
		Use:   "medreport360-viewer-build",
		Short: "building the MedReport package locally",
		Long: `This will build the medreport360-viewer portable-dist
	1. please run the command "medreport360-viewer-build input"to give the input values
		
	example commands:
		medreport360-viewer-build clean  -- "mvn clean install"
		medreport360-viewer-build build  -- "mvn clean package"

	If "mvn clean" is successfull the "mvn build" will be autorun without user input`,
	}
)
var EnableCommandSorting = true

// Execute executes the root command.
func Execute() error {
	rootCmd.CompletionOptions.DisableDefaultCmd = false
	// avbcomm()
	return rootCmd.Execute()

}

func init() {
	cobra.AddTemplateFunc("StyleHeading", color.New(color.FgGreen).SprintFunc())
	usageTemplate := rootCmd.UsageTemplate()
	usageTemplate = strings.NewReplacer(
		`Usage:`, `{{StyleHeading "Usage:"}}`,
		`Aliases:`, `{{StyleHeading "Aliases:"}}`,
		`Available Commands:`, `{{StyleHeading "Available Commands:"}}`,
		`Global Flags:`, `{{StyleHeading "Global Flags:"}}`,
		// The following one steps on "Global Flags:"
		// `Flags:`, `{{StyleHeading "Flags:"}}`,
	).Replace(usageTemplate)
	re := regexp.MustCompile(`(?m)^Flags:\s*$`)
	usageTemplate = re.ReplaceAllLiteralString(usageTemplate, `{{StyleHeading "Flags:"}}`)
	rootCmd.SetUsageTemplate(usageTemplate)
	// for autosuggestion of wrong entered commands
	rootCmd.DisableSuggestions = false
	rootCmd.SuggestionsMinimumDistance = 2

}

// func avbcomm() {
// 	color.Green("Available Commands: \n")

// }

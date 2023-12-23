package cmd

import (
	"bufio"
	"fmt"
	"log"
	"os/exec"
	"strings"

	"github.com/fatih/color"
	"github.com/spf13/cobra"
)

// var directory, _ = os.Getwd()
// var directory = &cobra.{}

// traceCmd represents the trace command
var cleanCmd = &cobra.Command{
	Use:   "clean",
	Short: "maven 1st build command 'MVN clean install' ",
	Long:  `maven clean install.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("running the 1st command")
		status, _ := cmd.Flags().GetString("status")

		fmt.Println(status)

		if status != "" {
			// status := "silent"
			fmt.Println("\n\n")
			cleanDistribution(status)
			// fmt.Println("not orinted")
		} else {
			// fmt.Println("orinted")
			cleanDistribution("something")
		}

	},
}

func init() {

	rootCmd.AddCommand(cleanCmd)

	cleanCmd.PersistentFlags().String("status", "", "will run silently, only success message will be shown, if failure occurs, the whole log will be printed")

}

// func readvalue1() (directory string) {
// 	read_directory, _ := os.ReadFile(userhome + "/.medbuild" + "/" + "config.txt")
// 	directory1 := string(read_directory)
// 	// config1.txt
// 	// read_portable, _ := os.ReadFile(userhome + "/.medbuild" + "/" + "config1.txt") //required from v4.0
// 	// portable = string(read_portable)
// 	// portable = "-Dportable=" + string(read_portable) // required from v4.0
// 	directory = strings.TrimSuffix(directory1, "\r")
// 	return directory
// }
func cleanDistribution(status string) {
	// status := "silent"
	directory, _ := readvalue()
	color.Cyan("\n1. Mvn clean install")
	// command := exec.Command("mvn", "clean", "install", "-Dchangelist=")
	command := exec.Command("mvn", "clean", "install", "-Dchangelist=")
	command.Dir = directory
	// output, err := command.Output()
	// debug
	fmt.Print("\n command :", command)
	fmt.Print("\n directory :", directory)
	fmt.Print("\n command.Dir :", command.Dir, "\n")

	if status == "silent" {
		output, _ := command.Output()
		lines := strings.Split(string(output), "\n")
		if strings.Contains(string(output), "ERROR") {
			for _, line := range lines {
				if strings.Contains(line, "ERROR") {

					if strings.Contains(line, "ERROR") {
						color.Red(line)
					}

				} else if strings.Contains(line, "WARNING") {
					color.Cyan(line)
				} else if strings.Contains(line, "BUILD SUCCESS") {
					color.Green(line)
				} else if strings.Contains(line, "BUILD FAILURE") {
					color.HiRed(line)
				} else {
					fmt.Println(line)
				}
			}
		} else {
			color.Green("\nMvn clean install was success\n\n\n")
		}
	} else {

		cmdReader, err := command.StdoutPipe()
		if err != nil {
			log.Fatal(err)
		}
		scanner := bufio.NewScanner(cmdReader)
		go func() {
			for scanner.Scan() {
				if strings.Contains(scanner.Text(), "WARNING") {
					color.Cyan(scanner.Text())
				} else if strings.Contains(scanner.Text(), "ERROR") {
					color.HiRed(scanner.Text())
				} else if strings.Contains(scanner.Text(), "BUILD SUCCESS") {
					color.Green(scanner.Text())
				} else {
					fmt.Println(scanner.Text())
				}
			}
		}()
		if err := command.Start(); err != nil {
			log.Fatal(err)
		}
		if err := command.Wait(); err != nil {
			log.Fatal(err)
		}
		if command.ProcessState.Success() { // ProcessState is set after Wait
			color.Green("\nMvn clean install was success")
			fmt.Println("\n\n\n")
		} else {
			color.Red("\nScript 'MVN clean install' failed")

		}

	}
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	// previous working-- no error found
	// cmdReader, err := command.StdoutPipe()
	// if err != nil {
	// 	log.Fatal(err)
	// }
	// scanner := bufio.NewScanner(cmdReader)
	// go func() {
	// 	for scanner.Scan() {
	// 		if strings.Contains(scanner.Text(), "WARNING") {
	// 			color.Cyan(scanner.Text())
	// 		} else if strings.Contains(scanner.Text(), "ERROR") {
	// 			color.HiRed(scanner.Text())
	// 		} else if strings.Contains(scanner.Text(), "BUILD SUCCESS") {
	// 			color.Green(scanner.Text())
	// 		} else {
	// 			fmt.Println(scanner.Text())
	// 		}
	// 	}
	// }()
	// if err := command.Start(); err != nil {
	// 	log.Fatal(err)
	// }
	// if err := command.Wait(); err != nil {
	// 	log.Fatal(err)
	// }
	// if command.ProcessState.Success() { // ProcessState is set after Wait
	// 	color.Green("Mvn clean install was success")
	// 	fmt.Println("\n\n\n")
	// 	// config3, _ := os.ReadFile(userhome + "/.medbuild/" + "config3.txt")
	// 	// if string(config3) == "Y" {
	// 	// 	actual_code()
	// 	// }
	// 	// fmt.Print("\nAutorun: ", yellow(string(config3)))

	// } else {
	// 	color.Red("Script 'MVN clean install' failed")

	// }

}

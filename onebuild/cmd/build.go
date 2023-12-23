/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>

*/
package cmd

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"

	"github.com/fatih/color"
	"github.com/spf13/cobra"
)

// buildCmd represents the build command
var buildCmd = &cobra.Command{
	Use:   "build",
	Short: "maven 2nd build command 'MVN clean package' ",
	Long:  `maven clean package.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("running the 2nd command")
		// packageDistribution()
		// directory, portable := readvalue()
		// fmt.Println(directory, portable)
		status, _ := cmd.Flags().GetString("status")
		if status != "" {
			// status := "silent"
			actual_code(status)
			// fmt.Println("not orinted")
		} else {
			// fmt.Println("orinted")
			actual_code("something")
		}

	},
}

// var directory, _ = os.Getwd()

func init() {

	rootCmd.AddCommand(buildCmd)
	buildCmd.PersistentFlags().String("status", "", "will run silently, only success message will be shown, if failure occurs, the whole log will be printed")

}

// mvn clean package -Dportable=true -P compressXZ -f weasis-distributions

func readvalue() (directory string, portable string) {
	read_directory, _ := os.ReadFile(userhome + "/.medbuild" + "/" + "config.txt")
	directory1 := string(read_directory)
	directory = strings.TrimSuffix(directory1, "\r")
	// config1.txt
	// read_portable, _ := os.ReadFile(userhome + "/.medbuild" + "/" + "config1.txt") //required from v4.0
	// portable = string(read_portable)
	// portable = "-Dportable=" + string(read_portable) // required from v4.0
	portable = "-Dportable=true"
	return directory, portable
}

//actual code run
func actual_code(status string) {
	// status := "silent"
	var portable = "-Dportable=true"
	directory, portable := readvalue()
	color.Cyan("\n\n\nrunning the cleanDistribution,\n 2.Mvn clean package")
	command := exec.Command("mvn", "clean", "package", "-Dchangelist=", portable, "-P", "compressXZ", "-f", "weasis-distributions") //"-P", "compressXZ", "-f", "weasis-distributions")
	command.Dir = directory
	// output, err := command.Output()
	fmt.Println(command)

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
			color.Green("\nMvn clean package was success\n\n\n")
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
			color.Green("\nMvn clean package was success \n\n\n")
			// config3, _ := os.ReadFile(userhome + "/.medbuild/" + "config3.txt")
			// if string(config3) == "Y" {
			// 	msi_installer()
			// }

		} else {
			color.Red("\nScript 'MVN clean package failed\n")
		}

	}

	// prev working commands
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
	// 	color.Green("MVN clean package was success \n\n\n")
	// 	// config3, _ := os.ReadFile(userhome + "/.medbuild/" + "config3.txt")
	// 	// if string(config3) == "Y" {
	// 	// 	msi_installer()
	// 	// }

	// } else {
	// 	color.Red("\nScript 'MVN clean package failed\n")
	// }

}

// for -Dportable true or false, useless untill v4.0
// func packageDistribution() (string, string) {

// 	// finding the directory
// 	// 1. input 2. local config.txt 3. os.getwd()
// 	pr_directory, _ := os.Getwd()
// 	_, err := os.Stat(userhome + "/.medbuild" + "/" + "config.txt")
// 	if os.IsNotExist(err) {
// 		_, err := os.Stat(pr_directory + "config.txt")
// 		if os.IsNotExist(err) {
// 			directory := pr_directory

// 		}
// 	}
// 	_, err = os.Stat(userhome + "/.medbuild" + "/" + "config1.txt")
// 	if os.IsNotExist(err) {
// 		_, err := os.Stat("config.txt")
// 		if os.IsNotExist(err) {
// 			// assuming the default values for portable dist i.e true
// 			portable := "-Dportable=true"
// 			actual_code(portable, directory)
// 		} else {
// 			// gettting the portable true or false
// 			config, err := ioutil.ReadFile("config.txt")
// 			body := string(config)
// 			// fmt.Println(body)
// 			if err != nil {
// 				// fmt.Println("error")
// 			}
// 			regex := regexp.MustCompile("(?m)(?P<name>portable)=(?P<value>\"....\")") // working extractable build number
// 			template := "$key=$value\n"
// 			result := []byte{}
// 			for _, submatches := range regex.FindAllStringSubmatchIndex(body, -1) {
// 				result = regex.ExpandString(result, template, body, submatches)
// 				// fmt.Println(submatches)
// 			}
// 			portable1 := string(result) // converting the byte to a string
// 			Portable_t := strings.Trim(portable1, "= \n")
// 			portable := "-Dportable=" + Portable_t
// 			actual_code(portable, directory)

// 		}
// 	} else {
// 		//for directory
// 		content, err := ioutil.ReadFile(userhome + "/.medbuild" + "/" + "config.txt")
// 		if err != nil {
// 			fmt.Println("config.txt not found in ", userhome)
// 			directory1 := (string(content))

// 			content1, err := ioutil.ReadFile(userhome + "/.medbuild" + "/" + "config1.txt")
// 			if err != nil {
// 				fmt.Println("config1.txt not found in ", userhome)
// 				value := "-Dportable=" + (string(content1))
// 				portable1 := value
// 				actual_code(portable1, directory1)
// 			}
// 		}

// 	}

// }

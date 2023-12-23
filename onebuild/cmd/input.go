/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>



config4 := gitbash_input
*/
package cmd

import (
	"bufio"
	"fmt"
	"os"
	"strings"

	"github.com/fatih/color"
	"github.com/spf13/cobra"
)

// inputCmd represents the input command
var inputCmd = &cobra.Command{
	Use:   "input",
	Short: "enter the path to the medreport root and JDK 18 path",
	Long:  `enter the path to the medreport root and JDK 18 path.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Printf("please enter the required values, \n to keep any value as same as previous, skip by pressing enter\n\n")
		input()
		// input1() //asking for -Dportable=true, or false // removing as it is not required and will be need from v4.0
		javapath_input()
		gitbash_input()
		// color.Cyan("\n\nconfigure Autorun")
		// color.Cyan("The Program when building \"clean command\" if sucessfull will autorun the next command \"Build command\" and then will build \"MSI\" if successful")
		// autorun()
	},
}

var userhome = os.Getenv("USERPROFILE")

func init() {
	rootCmd.AddCommand(inputCmd)

	// Here you will define your flags and configuration settings.

	// Cobra supports Persistent Flags which will work for this command
	// and all subcommands, e.g.:
	// inputCmd.PersistentFlags().String("foo", "", "A help for foo")

	// Cobra supports local flags which will only run when this command
	// is called directly, e.g.:
	// inputCmd.Flags().BoolP("toggle", "t", false, "Help message for toggle")
}

func input() {
	os.Mkdir(userhome+"/.medbuild", 0666)
	color.Cyan("Enter the root for medreport source: \n")
	fmt.Print("example: D:/JOB/WEASIS 2/Weasis-3.8.1_vanilla/Weasis-3.8.1\n")
	reader := bufio.NewReader(os.Stdin)
	// ReadString will block until the delimiter is entered
	input, err := reader.ReadString('\n')

	if err != nil {
		fmt.Println("An error occured while reading input. Please try again", err)
		return
	}
	// remove the delimeter from the string
	input = strings.TrimSuffix(input, "\n")

	if len(input) > 1 {

		fmt.Println("value entered is : ", input)

		file, err := os.OpenFile(userhome+"/.medbuild"+"/"+"config.txt", os.O_CREATE|os.O_WRONLY, 0666)
		if err != nil {
			fmt.Println("error in saving the config file for input root directory", err)

		}
		file.WriteString(input)
		fmt.Println("\n")
	} else {
		// yellow := color.New(color.FgCyan).SprintFunc()
		config, _ := os.ReadFile(userhome + "/.medbuild/" + "config.txt")
		fmt.Print("using the previous value")
		color.Cyan(string(config))
	}

}

// func input1() {
// 	color.Cyan("Enter the -Dportable value: \n")
// 	fmt.Print("example: true or false\n")
// 	reader := bufio.NewReader(os.Stdin)
// 	// ReadString will block until the delimiter is entered
// 	input, err := reader.ReadString('\n')
// 	if err != nil {
// 		fmt.Println("An error occured while reading input. Please try again", err)
// 		return
// 	}
// 	// remove the delimeter from the string
// 	input = strings.TrimSuffix(input, "\n")
// 	fmt.Println("value entered is", input)

// 	file, err := os.OpenFile(userhome+"/.medbuild"+"/"+"config1.txt", os.O_CREATE|os.O_WRONLY, 0666)
// 	file.WriteString(input)
// fmt.Println("\n\n")
// }

func javapath_input() {
	color.Cyan("\nEnter the path to JDK 18.0.1.1\n")
	fmt.Print("example: ..../jdk-18.0.1.1 \n")
	color.Cyan("(do not add slash as [.../jdk-18.0.1.1'/']  or  [jdk-18.0.1.1/bin] as path) \n")
	reader := bufio.NewReader(os.Stdin)
	input, err := reader.ReadString('\n')
	if err != nil {
		fmt.Println("An error occured while reading java_input. Please try again :", err)
		javapath_input()
		// return
	}
	// remove the delimeter from the string
	input = strings.TrimSuffix(input, "\n")
	if len(input) > 1 {

		fmt.Print("java_path entered is : ", input)

		file, err := os.OpenFile(userhome+"/.medbuild"+"/"+"config2.txt", os.O_CREATE|os.O_WRONLY, 0666)
		if err != nil {
			fmt.Println("error in saving the config file for JAVA_PATH", err)

		}
		file.WriteString(input)
		fmt.Println("\n")
	} else {
		// yellow := color.New(color.FgCyan).SprintFunc()
		config, _ := os.ReadFile(userhome + "/.medbuild/" + "config2.txt")
		fmt.Println("using the previous value")
		color.Cyan(string(config))
	}

}

func gitbash_input() {
	color.Cyan("\nEnter the path to Git-bash.exe\n")
	fmt.Print("example: ....C:/Program Files/Git/git-bash.exe \n")
	color.Cyan("(do not add  any other exe ( bash.exe, sh.exe, git-gui-exe ) as path) \n")
	reader := bufio.NewReader(os.Stdin)
	input, err := reader.ReadString('\n')
	if err != nil {
		fmt.Println("An error occured while reading gitbash input. Please try again :", err)
		gitbash_input()
		// return
	}
	// remove the delimeter from the string
	input = strings.TrimSuffix(input, "\n")
	if len(input) > 1 {

		fmt.Print("gitbash_input entered is : ", input)

		file, err := os.OpenFile(userhome+"/.medbuild"+"/"+"config4.txt", os.O_CREATE|os.O_WRONLY, 0666)
		if err != nil {
			fmt.Println("error in saving the config file for gitbash_input", err)

		}
		file.WriteString(input)
		fmt.Println("\n")
	} else {
		// yellow := color.New(color.FgCyan).SprintFunc()
		config, _ := os.ReadFile(userhome + "/.medbuild/" + "config4.txt")
		fmt.Println("using the previous value")
		color.Cyan(string(config))
	}

}

// func autorun() {

// 	color.Cyan("\nAutorun Enable? \n")
// 	fmt.Print("(Y for yes) or (N for no) \n")
// 	var input string
// 	fmt.Scanf("%s", &input)
// 	// remove the delimeter from the string
// 	input = strings.TrimSuffix(input, "\n")

// 	if len(input) >= 1 {

// 		if input == "Y" {
// 			fmt.Print("Autorun is Enabled")
// 		} else if input == "N" {
// 			fmt.Print("Autorun is Disabled")
// 		} else {
// 			fmt.Printf(input)
// 			fmt.Printf("wrong input, please re-enter the value properly \n")
// 			autorun()
// 		}

// 		file, err := os.OpenFile(userhome+"/.medbuild"+"/"+"config3.txt", os.O_CREATE|os.O_WRONLY, 0666)
// 		if err != nil {
// 			fmt.Println("error in saving the config file for autorun", err)

// 		}
// 		file.WriteString(input)
// 		fmt.Println("\n")
// 	} else {
// 		// yellow := color.New(color.FgCyan).SprintFunc()
// 		config, _ := os.ReadFile(userhome + "/.medbuild/" + "config3.txt")
// 		fmt.Println("using the previous value")
// 		color.Cyan(string(config))
// 	}
// }

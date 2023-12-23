/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>

*/
package cmd

import (
	"archive/zip"
	"bufio"
	"fmt"
	"io"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"strings"

	"github.com/fatih/color"
	"github.com/spf13/cobra"
)

// msiCmd represents the msi command
var msiCmd = &cobra.Command{
	Use:   "msi",
	Short: "To build the MSI Package",
	Long:  `To build the MSI Package.`,
	Run: func(cmd *cobra.Command, args []string) {

		msi_installer()
	},
}

func init() {
	rootCmd.AddCommand(msiCmd)

	// Here you will define your flags and configuration settings.

	// Cobra supports Persistent Flags which will work for this command
	// and all subcommands, e.g.:
	// msiCmd.PersistentFlags().String("foo", "", "A help for foo")
	msiCmd.PersistentFlags().String("silent", "", "will run silently, only success message will be shown, if failure occurs, the whole log will be printed")
	// Cobra supports local flags which will only run when this command
	// is called directly, e.g.:
	// msiCmd.Flags().BoolP("toggle", "t", false, "Help message for toggle")
}

func msi_installer() {
	fmt.Println("\nstarting the build of MSI installer\n")
	myBytes, _ := os.ReadFile(userhome + "/.medbuild/" + "config.txt") // configure the medreport root
	config1 := string(myBytes[:])
	config := strings.TrimSuffix(config1, "\r")

	packageweasis := config + "/weasis-distributions/script" //configure the script folder for package-weasis.sh

	target := config + "/weasis-distributions/target/portable-dist" // configure the portable-dist folder for the build package
	myBytes2, _ := os.ReadFile(userhome + "/.medbuild/" + "config2.txt")
	something := string(myBytes2[:])
	pathcustomresources := (config + "/weasis-distributions/custom_resources")
	myBytes4, _ := os.ReadFile(userhome + "/.medbuild/" + "config4.txt")
	git_path := string(myBytes4[:])

	jdk_path := strings.TrimSuffix(something, "\r") //configureing the jdk_path
	// step 1
	//unzipping the weasis-portable.zip package created
	_, err := Unzip(target+"/weasis-portable.zip", target)
	if err != nil {
		fmt.Println("error in unzipping the package", target+"/weasis-portable.zip")
	}

	// copying all the patch fils from custom resources to portable-dist
	buildupdater(pathcustomresources)
	copypatch(config)
	// actual command // Bash := "C:/Program Files/Git/git-bash.exe"
	//"C:/Program Files/Git/git-bash.exe"
	fmt.Println("\npath:\n", git_path)
	command := exec.Command(git_path, "package-weasis-modif.sh", "-i", target, "-j", jdk_path, "-o", config+"/weasis-distributions/target/native-dist")
	command.Dir = packageweasis
	color.Cyan("(Running script package-weasis.sh) \nRunning command: ")
	fmt.Print(command, "\n")
	// fmt.Println(err)
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
		color.Green("\nMSI Script ran sucessfully \n\n")
		color.Cyan("MSI Installer generated at: ")
		fmt.Println(config + "/weasis-distributions/target/native-dist")
		// fmt.Println("\n\n\n")

	} else {
		color.Red("MSI Build is failure \n The input values are ")

		info()
		fmt.Println("command to build the MSI", command)
		fmt.Println("Directory", command.Dir)
	}
}

func Unzip(src string, dest string) ([]string, error) {

	var filenames []string

	r, err := zip.OpenReader(src)
	if err != nil {
		return filenames, err
	}
	defer r.Close()

	for _, f := range r.File {

		// Store filename/path for returning and using later on
		fpath := filepath.Join(dest, f.Name)

		// Check for ZipSlip. More Info: http://bit.ly/2MsjAWE
		if !strings.HasPrefix(fpath, filepath.Clean(dest)+string(os.PathSeparator)) {
			return filenames, fmt.Errorf("%s: illegal file path", fpath)
		}

		filenames = append(filenames, fpath)

		if f.FileInfo().IsDir() {
			// Make Folder
			os.MkdirAll(fpath, os.ModePerm)
			continue
		}

		// Make File
		if err = os.MkdirAll(filepath.Dir(fpath), os.ModePerm); err != nil {
			return filenames, err
		}

		outFile, err := os.OpenFile(fpath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, f.Mode())
		if err != nil {
			return filenames, err
		}

		rc, err := f.Open()
		if err != nil {
			return filenames, err
		}

		_, err = io.Copy(outFile, rc)

		// Close the file without defer to close before next iteration of loop
		outFile.Close()
		rc.Close()

		if err != nil {
			return filenames, err
		}
	}
	return filenames, nil
}

// will copy patch files from custom_resources to portable-dist/weasis
func copypatch(config string) {
	// Bash := "C:/Program Files/Git/git-bash.exe" "--login"
	// robocopy src dst /E /z /R:3 /W:5 /TBD /eta /V /COPYALL /is /it /im /XF updater.go
	//"/COPYALL", err, ERROR : You do not have the Manage Auditing user right.
	command := exec.Command("robocopy", config+"/weasis-distributions/custom_resources", config+"/weasis-distributions/target/portable-dist/weasis", "/E", "/z", "/R:3", "/W:5", "/TBD", "/eta", "/V", "/is", "/it", "/im", "/XF", "updater.go")
	// /e	Copies subdirectories. This option automatically includes empty directories.
	// /mir	Mirrors a directory tree overwrites the destination directory security settings.
	// /is	Includes the same files. Same files are identical in name, size, times, and all attributes.
	// /it	Includes "tweaked" files. Tweaked files have the same name, size, and times, but different attributes.
	// /IM :: Include Modified files
	// /copyall	Copies all file information
	command.Dir = config + "/weasis-distributions/custom_resources" //pointing to customresources folder | custom_resources
	color.Cyan("(copying the patch files to portable dist) \n Running command: ")
	fmt.Println(command, "\n")
	// fmt.Println(err)
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
			} else if strings.Contains(scanner.Text(), "SUCCESS") {
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
		fmt.Println(err)
		myBytes := []byte(fmt.Sprintf("%s", err))

		//regex implementation
		body := string(myBytes)
		// fmt.Println(body)
		medversion := regexp.MustCompile("(?P<name>exit status)\\s(?P<value>[[:alnum:]$])") // working extractable build number
		template := "$key=$value\n"
		medresult := []byte{}
		for _, submatches := range medversion.FindAllStringSubmatchIndex(body, -1) {
			medresult = medversion.ExpandString(medresult, template, body, submatches)
			// fmt.Println(submatches)
		}
		med_version := string(medresult) // converting the byte to a string
		result := strings.Trim(med_version, "= \n")
		switch {
		case result == "1":
			color.Green("All files were copied successfully.")
			color.Green("\ncustom resources was copied sucessfully \n\n")
		case result == "2":
			color.HiRed("There are some additional files in the destination directory that are not present in the source directory. No files were copied.")
		case result == "3":
			color.Green("Some files were copied. Additional files were present. No failure was encountered.")
		case result == "5":
			color.HiYellow("Some files were copied. Some files were mismatched. No failure was encountered.")
		case result == "6":
			color.HiYellow("Additional files and mismatched files exist. No files were copied and no failures were encountered. This means that the files already exist in the destination directory.")
		case result == "7":
			color.HiRed("Files were copied, a file mismatch was present, and additional files were present.")
		case result == "8":
			color.HiRed("Several files did not copy.")
		case result == "0":
			color.HiRed("No files were copied. No failure was encountered. No files were mismatched. The files already exist in the destination directory; therefore, the copy operation was skipped.")

		}
	}
	//deleting the updater.exe post copying for a cleaner build
	os.Remove(config + "/weasis-distributions/custom_resources/updater.exe")

}

// will rebuild updater.go
func buildupdater(pathcustomresources string) {

	// env GOOS=windows GOARCH=amd64 go build updater.go
	command := exec.Command("go", "build", pathcustomresources+"/updater.go")
	command.Dir = pathcustomresources //pointing to customresources folder | custom_resources
	command.Env = append(os.Environ(), "GOOS=windows", "GOARCH=amd64")

	color.Cyan("(building updater.go) \n Running command: ")
	fmt.Print(command, "\n")
	// fmt.Println(" ", command.Env)
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
			} else if strings.Contains(scanner.Text(), "SUCCESS") {
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
		// color.Green("\nThe updater was built sucessfully \n\n")
		// fmt.Println("\n\n\n")

	} else {
		color.Red("failed to build the updater.go:", err)
		os.Exit(2)
	}
}

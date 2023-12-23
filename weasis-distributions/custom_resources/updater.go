// version - 1.2.3
// conditions -
// 1. URL on the server
// 2. Version info page on the server

// code conditions -
// 	1. URL: "http://viewer-updater.medreport360.com"
// 	2. VERSIONURL: "http://viewer-updater.medreport360.com/version_info.php"
// 	3. it works only if installed to C:/Program Files/MedReport360-Viewer

//version.txt sample
// VERSION:3.8.1
// BUILD-VERSION:35
// BUILD-NO:1005

package main

import (
	"archive/zip"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"os/exec"
	"path"
	"path/filepath"
	"regexp"
	"strings"
	"syscall"
	"time"
)

var (
	WarningLogger *log.Logger
	InfoLogger    *log.Logger
	ErrorLogger   *log.Logger
	SuccessLogger *log.Logger
)

// change here if anything related to where the logs should be placed or [installdir] or version.txt please change here
//should be in the same path as specified below

var INSTALLDIR = "C:/Program Files/MedReport360-Viewer"
var LOGFILE = "C:/Program Files/MedReport360-Viewer/app"
var VERSIONFILE = "C:/Program Files/MedReport360-Viewer/app"
var Temp = os.Getenv("temp")

// var revert_back1 bool

// production
var URL = "http://viewer-updater.medreport360.com"                         // patch_files_url
var URLVERSION = "http://viewer-updater.medreport360.com/version_info.php" // version_info_url
var ENVIRONMENT = "production"

// testing
// var URL = "http://localhost/demo" // patch_files_url
// var URLVERSION = "http://localhost/demo/version_info.php"
// var ENVIRONMENT = "testing"

func init() {
	file, err := os.OpenFile(LOGFILE+"/logs.txt", os.O_APPEND|os.O_WRONLY, 0666)
	if err != nil {
	}

	//creating the log file
	_, err = os.Stat(LOGFILE + "/" + "logs.txt")

	// check if logs.txt exist, if not create a new one"
	if os.IsNotExist(err) {
		os.Create(LOGFILE + "/" + "logs.txt")
		file.WriteString(fmt.Sprintf("\n\n\n"))
		// file.WriteString(fmt.Sprintf("===================The log file was not not found in the DIR: %d===================\n", LOGFILE))
		file.WriteString(fmt.Sprintf("================================Creating a new log file===================\n"))
	} else {
		// file.WriteString(fmt.Sprintf("==========================Prev Log found, appending to the same===================\n"))
	}

	if ENVIRONMENT == "testing" {
		InfoLogger = log.New(file, "INFO:    ", log.Ldate|log.Ltime)
		SuccessLogger = log.New(file, "SUCCESS: ", log.Ldate|log.Ltime)
		WarningLogger = log.New(file, "WARNING: ", log.Ldate|log.Ltime)
		ErrorLogger = log.New(file, "ERROR:    ", log.Ldate|log.Ltime)
	} else {
		InfoLogger = log.New(file, "INFO:    ", log.Ldate|log.Ltime)
		SuccessLogger = log.New(file, "SUCCESS: ", log.Ldate|log.Ltime)
		WarningLogger = log.New(file, "WARNING: ", log.Ldate|log.Ltime)
		ErrorLogger = log.New(file, "ERROR:    ", log.Ldate|log.Ltime)
	}
}

// LOGGER - use case
// InfoLogger.Println("Starting the application...")
// InfoLogger.Println("Something noteworthy happened")
// WarningLogger.Println("There is something you should know about")
// ErrorLogger.Println("Something went wrong")

func main() {
	// checking on the server if any new version exists

	parser, err := http.Get(URLVERSION) //version_info.php
	if err != nil {
		ErrorLogger.Println("error in parsing the version URL")
		log.Fatal(err)
	}
	defer parser.Body.Close()

	// Read the patch version from URL in to memory
	patch_content, err := ioutil.ReadAll(parser.Body)
	if err != nil {
		ErrorLogger.Println("Error reading version post URL parsing. ", err)
	}
	body := string(patch_content)

	// =====================================================================

	// get the latest version numbers from the server

	// VERSION
	medversion := regexp.MustCompile("(?m)(?P<name>VERSION):(?P<value>\\d.\\d.\\d+)") // getting the VERSION number from the server
	template := "$key=$value\r\n"
	medresult := []byte{}
	for _, submatches := range medversion.FindAllStringSubmatchIndex(body, -1) {
		medresult = medversion.ExpandString(medresult, template, body, submatches)
	}
	med_version := string(medresult)                                 // converting the byte to a string
	med_latest_patch_version := strings.Trim(med_version, "= \r \n") // removign the character "="

	// BUILD-VERSION
	re := regexp.MustCompile("(?m)(?P<name>BUILD-VERSION):(?P<value>\\d\\d+)") // getting the BUILD-VERSION from the server
	result := []byte{}
	for _, submatches := range re.FindAllStringSubmatchIndex(body, -1) {
		result = re.ExpandString(result, template, body, submatches)
	}
	patch_version := string(result)
	latest_patch_version := strings.Trim(patch_version, "= \r \n")

	// BUILD-NO
	rc := regexp.MustCompile("(?m)(?P<name>BUILD-NO):(?P<value>\\d\\d+)") // getting the BUILD-NO from the server
	// fmt.Printf(rc.)
	template = "$key=$value\r\n"
	buildnoresult := []byte{}
	for _, submatches := range rc.FindAllStringSubmatchIndex(body, -1) {
		buildnoresult = rc.ExpandString(buildnoresult, template, body, submatches)
	}
	buildno_version := string(buildnoresult)
	buildno_latest_patch_version := strings.Trim(buildno_version, "= \r \n")

	// =====================================================================

	// gettting the local version number present on system
	// from version.txt at "program files/medreport360-viewer/app/version.txt"

	createversion() // will create version.txt if not present by using the values in app/conf/config.properties
	local_content, _ := ioutil.ReadFile(VERSIONFILE + "/version.txt")

	// VERSION
	getversion := regexp.MustCompile("(?m)(?P<name>VERSION):(?P<value>\\d.\\d.\\d+)")
	versionresult := []byte{}
	for _, submatches := range getversion.FindAllStringSubmatchIndex(string(local_content), -1) {
		versionresult = getversion.ExpandString(versionresult, template, string(local_content), submatches)
	}
	version_string := string(versionresult)
	localversion := string(strings.Trim(version_string, "= \r \n"))

	// BUILD-NO
	getbuild := regexp.MustCompile("(?m)(?P<name>BUILD-NO):(?P<value>\\d\\d+)")
	buildresult := []byte{}
	for _, submatches := range getbuild.FindAllStringSubmatchIndex(string(local_content), -1) {
		buildresult = getbuild.ExpandString(buildresult, template, string(local_content), submatches)
	}
	build_version_string := string(buildresult)
	build_local_version := string(strings.Trim(build_version_string, "= \r \n"))

	// BUILD-VERSION
	local_build_version1 := regexp.MustCompile("(?m)(?P<name>BUILD-VERSION):(?P<value>\\d\\d+)")
	local_build_version_result := []byte{}
	for _, submatches := range local_build_version1.FindAllStringSubmatchIndex(string(local_content), -1) {
		local_build_version_result = re.ExpandString(local_build_version_result, template, string(local_content), submatches)
	}
	local_build_version := strings.Trim(string(local_build_version_result), "= \r \n")

	// =====================================================================

	// if web version > local version OR web build_version > local build number OR web BUILD-NO > local BUILD-NO
	// if 3.9.1 > 3.8.1 || 32 > 31 || 1003 > 1002
	if latest_patch_version > local_build_version || buildno_latest_patch_version > build_local_version || med_latest_patch_version > localversion {
		// InfoLogger.Println(fmt.Sprintf("found New patch (%s) for MedReport360-Viewer v%s", latest_patch_version, med_latest_patch_version))
		delete_temp_files(latest_patch_version, build_local_version) // will delete prev temp files in %temp% (zip and version_64_patch)
		patcher(latest_patch_version, localversion, buildno_latest_patch_version)
		SuccessLogger.Println(fmt.Sprintf("successfully patched to the latest BUILD-VERSION %s with build_no %s, for MedReport360-Viewer v%s", latest_patch_version, buildno_latest_patch_version, med_latest_patch_version))
		changeversiontxt(latest_patch_version, buildno_latest_patch_version, med_latest_patch_version)
		InfoLogger.Println("updated the version and conf")
		// InfoLogger.Println("terminating the script")
		old_logs()
		// }
		os.Exit(0)
	} else {
		// // disabling the logger when there is no patch available
		// InfoLogger.Println(fmt.Sprintf("Already the latest patch %s for MedReport360-Viewer v%s running", localversion, med_latest_patch_version))
		// InfoLogger.Println(fmt.Sprintf("Revert_back specified as: %v, terminating the script here", revert_back1))
		// InfoLogger.Println("both are same version")
		os.Exit(0)
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

func patcher(latest_patch_version string, localversion string, buildno_latest_patch_version string) {

	InfoLogger.Println("starting the patching")
	patch_url := (URL + "/patch_" + latest_patch_version + "_" + buildno_latest_patch_version + ".zip")
	patching(patch_url)

	// deleting the patch file (patch_31_1001.zip)
	localfilepath := (Temp + "/patch_" + latest_patch_version + "_" + buildno_latest_patch_version + ".zip")
	os.RemoveAll(localfilepath)

	// deleting the patch folder (viewer_64_patch)
	tempfilepath := (Temp + "/" + "viewer_64_patch")
	os.RemoveAll(tempfilepath)
	// SuccessLogger.Println("successfully removed the patchfile: ", localfilepath)

}

func patching(patch_url string) {
	// will download the zip file according to the latest patch and store it in %temp%

	parser, _ := http.NewRequest("GET", patch_url, nil)
	fileName := path.Base(parser.URL.Path)

	// creating a temp empty file (patch_31_1001.zip) acc to latest patch
	out, err := os.Create(Temp + "/" + fileName)
	if err != nil {
		ErrorLogger.Println("error in creating a new local file in [windows temporary directory] ")
		log.Fatalln(err)
	}
	defer out.Close()

	// checking the URL for access or active
	resp, err := http.Get(patch_url)
	if err != nil {
		ErrorLogger.Println("The URL is not accessible, URL:", patch_url)
		log.Fatalln(err)
	}
	// the file will not have the correct url and is downloading some random corrupt file, and not showing the error
	defer resp.Body.Close()
	// InfoLogger.Println("URl is accessible, URL:", patch_url)

	// writing/downloading to the temp file
	out1, err := io.Copy(out, resp.Body)
	if err != nil {
		ErrorLogger.Println("error in downloading the patch.zip")
		log.Fatalln(err)
	}
	// InfoLogger.Println("successfull in writing/downloading the patch to the local windows Temp folder", out1)
	InfoLogger.Println(out1)

	// unzipping in the directory
	_, err = Unzip(Temp+"/"+fileName, Temp)
	if err != nil {
		ErrorLogger.Println("error in unzipping the file")
		log.Fatalln(err)
	}
	// InfoLogger.Println("successfull in unzipping the file to the local windows Temp folder")

	//running the patch.bat file from the patch file
	cmd := exec.Command("cmd", "/C", Temp+"/viewer_64_patch/patch.bat")
	cmd.Env = os.Environ()
	err = cmd.Run()
	// fmt.Printf("The o/p is %s\n", cmd)

	if err != nil {
		ErrorLogger.Println("Error: ", err)
		log.Fatal(err)
	}
	// Error:  exit status 1

}
func createversion() {
	// will create a version.txt in the [INSTALLDIR]/app with the version available on the system sourced from app/conf/config.properties

	// will check if both the version, build-version and build-no exists in version.txt if not will delete the whole file, reason: may end up as glitch so resolving here
	// possible bug, there might not be any values assciated, and it wont delete the file

	file, err1 := os.ReadFile(VERSIONFILE + "/version.txt")
	if os.IsExist(err1) {
		if strings.Contains(string(file), "VERSION:") && (strings.Contains(string(file), "BUILD-NO:")) {

		} else {
			os.Remove(VERSIONFILE + "/version.txt")
		}
	}
	_, err := os.OpenFile(VERSIONFILE+"/version.txt", os.O_APPEND|os.O_WRONLY, 0666)

	if os.IsNotExist(err) {
		ErrorLogger.Println("Version.txt not available, creating a new file")
		conf_file := INSTALLDIR + "/app/conf/config.properties"
		input, _ := ioutil.ReadFile(conf_file)
		lines := strings.Split(string(input), "\n")

		// weasis.version OR version 3.8.1
		for i, _ := range lines { //(i < 174 && i > 160)
			confversion := regexp.MustCompile("(?m)(?P<name>weasis.version)=(?P<value>\\d\\.\\d\\.\\d+)") // working extractable build number
			template := "$key=$value\r\n"
			confresult := []byte{}
			for _, submatches := range confversion.FindAllStringSubmatchIndex(lines[i], -1) {
				confresult = confversion.ExpandString(confresult, template, lines[i], submatches)
			}
			conf_version_string := string(confresult)
			conf_version := strings.Trim(conf_version_string, "= \r \n")
			write, _ := os.OpenFile(VERSIONFILE+"/version.txt", os.O_CREATE|os.O_WRONLY, 0666)
			write.WriteString("VERSION:" + conf_version)
		}

		// BUILD-VERSION 36
		for i, _ := range lines { //(i < 174 && i > 160)
			confversion := regexp.MustCompile("(?m)(?P<name>weasis.version)=\\.?\\d*\\.?\\d*\\.\\d-(?P<value>\\d\\d+)") // working extractable build number
			template := "$key=$value\r\n"
			confresult := []byte{}
			for _, submatches := range confversion.FindAllStringSubmatchIndex(lines[i], -1) {
				confresult = confversion.ExpandString(confresult, template, lines[i], submatches)
			}
			conf_version_string := string(confresult)
			conf_version := strings.Trim(conf_version_string, "= \r \n")
			write, _ := os.OpenFile(VERSIONFILE+"/version.txt", os.O_CREATE|os.O_WRONLY, 0666)
			write.WriteString("\nBUILD-VERSION:" + conf_version)
		}

		// BUILD-NO 1006
		for i, _ := range lines { //(i < 174 && i > 160)
			buildversion := regexp.MustCompile("(?m)(?P<name>BUILD-NO)=(?P<value>\\d+)")
			template := "$key=$value\r\n"
			buildresult := []byte{}
			for _, submatches := range buildversion.FindAllStringSubmatchIndex(lines[i], -1) {
				buildresult = buildversion.ExpandString(buildresult, template, lines[i], submatches)
			}
			build_version_string := string(buildresult)
			build_version := strings.Trim(build_version_string, "= \r \n")

			if build_version != "" {
				write, _ := os.OpenFile(VERSIONFILE+"/version.txt", os.O_APPEND|os.O_RDWR, 0666)
				write.WriteString("\nBUILD-NO:" + build_version)
			}
		}

	}
}
func changeversiontxt(latest_patch_version string, buildno_latest_patch_version string, med_latest_patch_version string) {
	// post patching, this will be run
	// will change the version.txt to the latest version patched
	file, err := os.OpenFile(VERSIONFILE+"/version.txt", os.O_CREATE|os.O_WRONLY, 0666)
	if err != nil {
		ErrorLogger.Println("failed to read&change the version.txt in DIR", VERSIONFILE+"/version.txt")
	}
	file.Truncate(0)
	// file.Seek(0, 0)
	file.WriteString("VERSION:" + med_latest_patch_version + "\r\nBUILD-VERSION:" + latest_patch_version + "\r\nBUILD-NO:" + buildno_latest_patch_version)
}

func delete_temp_files(latest_patch_version string, build_local_version string) {
	// will delete all the files related to our patching, prior starting the patching
	temp_patch_file := Temp + "/patch_" + latest_patch_version + "_" + build_local_version + ".zip"
	temp_patch_folder := Temp + "\\" + "viewer_64_patch"
	tasks := []string{temp_patch_file, temp_patch_folder}
	for _, s := range tasks {
		_, err := os.Stat(s)
		if os.IsNotExist(err) {
			continue
		} else {
			os.RemoveAll(s)
		}

	}
}

func old_logs() {
	// will delete the old logs
	// will run on jan 1-5  - will rename the present logs to old_logs, after 1 month of renaming it will delete the old_logs.txt
	var filesize int64 = 1048576 //1mb
	// var filesize int64 = 10485760 // 10mb
	// var filesize int64 = 8586 // 8kb

	new_file := LOGFILE + "/logs.txt"
	old_file := LOGFILE + "/old_logs.txt"

	// to find the size of the initial log
	file1, _ := os.Stat(new_file) // logs.txt
	size := file1.Size()

	// running only on january every year from day 1 - day 5
	_, month, day := time.Now().Date()
	if (day >= 1 && day <= 5) && month == 1 { //|| (day >= (01) && day <= 5) && month == 8 { //to run in 6 months or 1 year
		// renaming the logs.txt to old_logs.txt if greater than 10mb
		if size >= filesize {
			os.Rename(new_file, old_file)
			time, date, month := time.Now().Date()
			WarningLogger.Println("Time for old_logs deletion is 1 month from:", month, date, time)
			return
		}
	} else if (day >= 1 && day <= 5) && month == 7 { //to run in 6 months
		// renaming the logs.txt to old_logs.txt if greater than 10mb
		if size >= filesize {
			os.Rename(new_file, old_file)
			time, date, month := time.Now().Date()
			WarningLogger.Println("Time for old_logs deletion is 1 month from:", month, date, time)
			return
		}
	}
	// to find the time from old_logs.txt creation
	file2, err := os.Stat(old_file) // old_logs.txt
	if os.IsNotExist(err) {
		return
	}
	// checking the duration from creation of old_logs.txt
	d := file2.Sys().(*syscall.Win32FileAttributeData)
	cTime := time.Unix(0, d.CreationTime.Nanoseconds()) // output : 2022-07-30 15:43:00.7275458 +0530 IST
	// fmt.Println(cTime)

	// if older than 1month then will be deleted
	if time.Now().Sub(cTime) > 700*time.Hour {
		os.RemoveAll(old_file) //old_logs.txt
		WarningLogger.Println("Deleted the old_logs.txt, with last Modified Date: ", cTime)
		return
	}

}

// Updater
// We can get the url of installed with place and location and files if necessary

// The URL format should be
// 	var INSTALLDIR = "C:/Program Files/MedReport360-Viewer"
// 	var LOGFILE = "C:/Program Files/MedReport360-Viewer/app"
// 	var VERSIONFILE = "C:/Program Files/MedReport360-Viewer/app"

// 	var URL = "http://localhost/demo"       //location where the files are located on the server
// 	var URLVERSION = "http://localhost/demo/version_info.php" 	//the version entries

// 	var URL = "http://viewer-updater.medreport360.com"      //location where the files are located on the server
// 	var URLVERSION = "http://viewer-updater.medreport360.com/version_info.php"     //the version entries

// 			VERSION: 3.8.1 BUILD-VERSION: 31 REVERT_BACK: TRUE BUILD-NO: 1001
// 			You can interchange the location of the above, but these have to be the exact same in names

// The file version.txt ( should have the following contents )
// 	1.  will autocreate if not present, based on the /app/conf/config.properties
// 	2. If present and updating, it will be changed based on the latest update being made
// VERSION: 32
// BUILD-NO: 1001

// ==============================================
// =========Things to check on new patch=========
// ==============================================

// Patch to medreportviewer
// 	Zip file name should be
// 		patch_versionNo_revisionNo.zip
// 		patch_31_1001.zip  >   viewer_64_patch    >    patch contents
// 	App/conf/config.properties
// 		Line 172 = weasis.version=3.8.1-30 ( or automation can be done here )
// 		Line 173 = BUILD-NO=1001

// Patch to medreport auto updater
// 	The patch name should be updater_new.exe
// 		Should be placed in app/updater_new.exe

// Patch_url = localhost/demo/filename

// dlv version = 1.9.0 / go version = 1.18.5
// bug fixed = the version.txt contents was not getting fully replaced, hence now using os.Trunc to truncate the byte size

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
	"strconv"
	"strings"
)

var (
	WarningLogger *log.Logger
	InfoLogger    *log.Logger
	ErrorLogger   *log.Logger
	SuccessLogger *log.Logger
)

var INSTALLDIR = "C:/Program Files/MedReport360-Viewer"
var LOGFILE = "C:/Program Files/MedReport360-Viewer/app"
var VERSIONFILE = "C:/Program Files/MedReport360-Viewer/app"
var Temp = os.Getenv("temp")
var URL = "http://viewer-updater.medreport360.com/"                        // patch_files_url
var URLVERSION = "http://viewer-updater.medreport360.com/version_info.php" // version_info_url

// var presentversion int64 = 31
// var max int64 = 51
var revert_back1 bool

// var rb_value string

func init() {
	file, err := os.OpenFile(LOGFILE+"/logs.txt", os.O_APPEND|os.O_WRONLY, 0666)
	if err != nil {
		// ErrorLogger.Println("log file append error")
		// fmt.Println("log file append error")
		// changed here
	}

	//creating the log file
	_, err = os.Stat(LOGFILE + "/" + "logs.txt")
	// fmt.Println("Is logs.txt present in the directory", status)
	// check if logs.txt exist, if not create a new one"
	if os.IsNotExist(err) {
		os.Create(LOGFILE + "/" + "logs.txt")
		file.WriteString(fmt.Sprintf("\n\n\n"))
		file.WriteString(fmt.Sprintf("===================The log file was not not found in the DIR: %d===================\n", LOGFILE))
		file.WriteString(fmt.Sprintf("================================Creating a new file===================\n"))
		// ------------------------------if possible try to append the text to the existingg file
	} else {
		file.WriteString(fmt.Sprintf("\n\n\n"))
		file.WriteString(fmt.Sprintf("==========================Prev Log found, appending to the same===================\n"))
	}

	InfoLogger = log.New(file, "INFO:    ", log.Ldate|log.Ltime|log.Lshortfile)
	SuccessLogger = log.New(file, "SUCCESS: ", log.Ldate|log.Ltime|log.Lshortfile)
	WarningLogger = log.New(file, "WARNING: ", log.Ldate|log.Ltime|log.Lshortfile)
	ErrorLogger = log.New(file, "ERROR:    ", log.Ldate|log.Ltime|log.Lshortfile)
}

// LOGGER - use case
// InfoLogger.Println("Starting the application...")
// InfoLogger.Println("Something noteworthy happened")
// WarningLogger.Println("There is something you should know about")
// ErrorLogger.Println("Something went wrong")

// func build_no() {
func main() {
	// checking on the server if any new version exists
	//parser, _ := http.NewRequest("GET", URL, nil)
	// filename := path.Base(parser.URL.Path)
	// filename := "MedReport360-viewer-36.zip"
	// InfoLogger.Println(fmt.Println("file name parsed from the URL: ", filename))

	// gettting the present local version number
	local_content, err := ioutil.ReadFile(VERSIONFILE + "/version.txt")
	if err != nil {
		ErrorLogger.Println("The version.txt could not be found")
		log.Fatal(err)
	}
	localversion := string(local_content)
	// fmt.Println(localversion)
	//done

	//parsing the URL for latest version
	parser, err := http.Get(URLVERSION)
	if err != nil {
		ErrorLogger.Println("error in parsing the version URL")
		log.Fatal(err)
	}
	defer parser.Body.Close()

	// Read the patch version from URL in to memory
	patch_content, err := ioutil.ReadAll(parser.Body)
	if err != nil {
		ErrorLogger.Println("Error reading version in URL parsing. ", err)
	}
	body := string(patch_content)
	// fmt.Println(body)

	//===============error================ v 3.8.1\n has \n
	//getting the version of medreportviewer
	medversion := regexp.MustCompile("(?m)(?P<name>VERSION):\\s(?P<value>\\w+\\.?\\d*\\.?\\d*)") // working extractable build number
	template := "$key=$value\n"
	medresult := []byte{}
	for _, submatches := range medversion.FindAllStringSubmatchIndex(body, -1) {
		medresult = medversion.ExpandString(medresult, template, body, submatches)
		// fmt.Println(submatches)
	}
	med_version := string(medresult)                              // converting the byte to a string
	med_latest_patch_version := strings.Trim(med_version, "= \n") // removign the character "="
	// fmt.Println(med_latest_patch_version)
	// fmt.Println(med_version)

	// extracting the patch number from the URL
	// pattern for checking the numericals in the URL file name
	re := regexp.MustCompile("(?m)(?P<name>BUILD):\\s+?(?P<value>\\w+)") // working extractable build number
	// template := "$key=$value\n"
	result := []byte{}
	for _, submatches := range re.FindAllStringSubmatchIndex(body, -1) {
		result = re.ExpandString(result, template, body, submatches)
		// fmt.Println(submatches)
	}
	patch_version := string(result)                             // converting the byte to a string
	latest_patch_version := strings.Trim(patch_version, "= \n") // removign the character "="
	// latest_patch_version := strings.Trim(patch_version, "\n")
	// fmt.Println(latest_patch_version)
	// fmt.Println(patch_version)
	//done parsing, latest_patch_version is the ouput

	//                        error "=false"
	// checking for revertback
	rb := regexp.MustCompile("(?m)(?P<name>REVERT_BACK):\\s+?(?P<value>\\w+)") // working extractable build number
	revert_result := []byte{}
	for _, revertmatch := range rb.FindAllStringSubmatchIndex(body, -1) {
		result = re.ExpandString(revert_result, template, body, revertmatch)
		//fmt.Println(revertmatch)
	}
	revert := string(result)                 // converting the byte to a string
	rb_value := strings.Trim(revert, "= \n") // removign the character "="
	// rb_value1 := strings.Trim(rb_value, "\n")
	// fmt.Println(latest_patch_version)
	// fmt.Println(rb_value)
	// fmt.Println(rb_value1)
	// rb_value := true
	revert_back1, _ = strconv.ParseBool(rb_value)
	// fmt.Println(revert_back1)

	// checking whether to patch or not
	// ====================changed here =============================================
	//==============================================
	//===========================================
	// if latest_patch_version > localversion {
	// 	InfoLogger.Println(fmt.Sprintf("found New patch (%s) for MedReport360-Viewer v%s", latest_patch_version, med_latest_patch_version))
	// 	patcher()
	// 	SuccessLogger.Println(fmt.Sprintf("successfully patched to the latest version %d for MedReport360-Viewer v%d", latest_patch_version, med_latest_patch_version))
	// 	InfoLogger.Println("terminating the script")
	// 	os.Exit(0)
	// } else if revert_back1 == true {
	// 	InfoLogger.Println("Revert_back specified as TRUE, reverting to version: ", localversion)
	// 	revert_back()
	// 	InfoLogger.Println("Revert_back successfull, terminating the script")
	// 	os.Exit(0)
	// } else {
	// 	InfoLogger.Println("Already the latest patch %s for MedReport360-Viewer v%s running", localversion, med_latest_patch_version)
	// 	InfoLogger.Println("Revert_back not specified, terminating the script here")
	// 	os.Exit(0)
	// }

	if revert_back1 {
		InfoLogger.Println("Revert_back specified as TRUE, reverting to version: ", latest_patch_version)
		revert_back(latest_patch_version, med_latest_patch_version, localversion)
		InfoLogger.Println("Revert_back successfull, terminating the script")
		updateversion(latest_patch_version)
		conf_rename(med_latest_patch_version, latest_patch_version, localversion)
		InfoLogger.Println("updated the version.txt and conf.properties file")
		os.Exit(0)
	} else if latest_patch_version > localversion {
		InfoLogger.Println(fmt.Sprintf("found New patch (%s) for MedReport360-Viewer v%s", latest_patch_version, med_latest_patch_version))
		patcher(latest_patch_version, localversion)
		SuccessLogger.Println(fmt.Sprintf("successfully patched to the latest version %s for MedReport360-Viewer v%s", latest_patch_version, med_latest_patch_version))
		InfoLogger.Println("terminating the script")
		updateversion(latest_patch_version)
		conf_rename(med_latest_patch_version, latest_patch_version, localversion)
		InfoLogger.Println("updated the version.txt and conf.properties file")
		os.Exit(0)
	} else {
		InfoLogger.Println(fmt.Sprintf("Already the latest patch %s for MedReport360-Viewer v%s running", localversion, med_latest_patch_version))
		InfoLogger.Println("Revert_back not specified, terminating the script here")
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

func revert_back(latest_patch_version string, med_latest_patch_version string, localversion string) {
	// here when reverting to previous version, the prev version zip should be available, how will the URL of the zip be specified,
	// or if checking from a list of files on the server?
	// fmt.Println("reverting back")
	// 1. patch shld download to the temp fiolder and extract there
	// 2. should run the script from the patch as admin
	// 3. modify the script file

	file, _ := os.OpenFile(LOGFILE+"/"+"logs.txt", os.O_APPEND|os.O_WRONLY, 0666)
	file.WriteString(fmt.Sprintf("\n========================================Reverting to the Previous Version============================="))
	file.WriteString(fmt.Sprintf("\n==========================================Base Version %s================================", localversion))
	file.WriteString(fmt.Sprintf("\n==================================The Revert_back version is %s================================", latest_patch_version))
	file.WriteString(fmt.Sprintf("\n==========================The INSTALLDIR is: %s=======================", INSTALLDIR))
	file.WriteString(fmt.Sprintf("\n====================The URL is: %s=====================\n", URL))

	WarningLogger.Println("Deleting all the files in ", INSTALLDIR)
	prevversion_filepath := (INSTALLDIR)
	os.RemoveAll(prevversion_filepath + "/" + "app")
	// add a random code to check if deleted or not || osremoveall doesnt accept a value
	// if err != nil {
	// 	ErrorLogger.Println("error in deleting all the files in : ", prevversion_filepath)
	// 	log.Fatalln(err)
	// }
	SuccessLogger.Println("successfully deleted all the files in : ", prevversion_filepath+"/"+"app")
	InfoLogger.Println("starting the patching func")
	patch_url := (URL + "_" + latest_patch_version + ".zip")
	// fmt.Println(patch_url)
	patching(patch_url)
	localfilepath := (Temp + "/patch_" + latest_patch_version)
	os.RemoveAll(localfilepath)
	SuccessLogger.Println("successfully removed the patchfile: ", localfilepath)
	SuccessLogger.Println("patching func was success")

}

func patcher(latest_patch_version string, localversion string) {
	file, _ := os.OpenFile(LOGFILE+"/"+"logs.txt", os.O_APPEND|os.O_WRONLY, 0666)
	file.WriteString(fmt.Sprintf("\n========================================patching the installer============================="))
	file.WriteString(fmt.Sprintf("\n==========================================Base Version %s================================", localversion))
	file.WriteString(fmt.Sprintf("\n==========================The INSTALLDIR is: %s=======================", INSTALLDIR))
	file.WriteString(fmt.Sprintf("\n====================The URL is: %s=====================\n", URL))

	InfoLogger.Println("starting the patching func")
	patch_url := (URL + "_" + latest_patch_version + ".zip")
	patching(patch_url)
	localfilepath := (Temp + "/patch_" + latest_patch_version)
	os.RemoveAll(localfilepath)
	SuccessLogger.Println("successfully removed the patchfile: ", localfilepath)
	SuccessLogger.Println("patching func was success")

}

func patching(patch_url string) {
	//parsing the url for the specific versioned patch
	parser, _ := http.NewRequest("GET", patch_url, nil)
	fileName := path.Base(parser.URL.Path)
	// fmt.Println(fileName)
	// CREATING THE local file
	// -------------------------------read from the registry for the input path and give as an input here   ( not done )
	out, err := os.Create(Temp + "/" + fileName)
	if err != nil {
		ErrorLogger.Println("error in creating a new local file in [windows temporary directory] ")
		log.Fatalln(err)
	}
	defer out.Close()
	InfoLogger.Println("Successfully created a local file to write in :", out)

	// checking the URL for access or active
	resp, err := http.Get(patch_url)
	if err != nil {
		ErrorLogger.Println("The URL is not accessible, URL:", patch_url)
		log.Fatalln(err)
	}
	// -------------------------------the file on wrong url is downloading some random corrupt file, and not showing the error
	defer resp.Body.Close()
	InfoLogger.Println("URl is accessible, URL:", patch_url)

	// writing/downloading to the file
	out1, err := io.Copy(out, resp.Body)
	if err != nil {
		ErrorLogger.Println("error in downloading the patch.zip")
		log.Fatalln(err)
	}
	InfoLogger.Println("successfull in writing/downloading the patch to the local windows Temp folder", out1)

	// unzipping in the directory
	_, err = Unzip(Temp+"/"+fileName, Temp)
	if err != nil {
		ErrorLogger.Println("error in unzipping the file")
		log.Fatalln(err)
	}
	InfoLogger.Println("successfull in patching the file to the local windows Temp folder")

	//running the patch.bat file inside the script
	cmd := exec.Command("cmd", "/C", Temp+"/viewer_64_patch_v31/patch.bat")
	cmd.Env = os.Environ()
	err = cmd.Run()
	// fmt.Printf("The o/p is %s\n", cmd)

	if err != nil {
		ErrorLogger.Println("Error: ", err)
		// log.Fatal(err)
	}

	// error: still not able to delete the patch file, as of now, using the cmd to delete the file
	// deleting the patch file
	localfilepath := (Temp + "/" + "viewer_64_patch")

	os.RemoveAll(localfilepath)
	SuccessLogger.Println("successfully removed the patchfolder: ", localfilepath)

	// ErrorLogger.Println("could not remove the patch file: ", localfilepath)
	// log.Fatalln(err)
}
func updateversion(latest_patch_version string) {

	file, err := os.OpenFile(VERSIONFILE+"/version.txt", os.O_CREATE|os.O_WRONLY, 0666)
	if err != nil {
		ErrorLogger.Println("failed to read&change the version.txt in DIR", VERSIONFILE+"/version.txt")
	}
	file.WriteString(latest_patch_version)
	SuccessLogger.Println("updated version.txt to: ", latest_patch_version)
}

func conf_rename(med_latest_patch_version string, latest_patch_version string, localversion string) {
	conf_file := INSTALLDIR + "/app/conf/config.properties"
	input, err := ioutil.ReadFile(conf_file)
	if err != nil {
		log.Fatalln(err)
	}

	lines := strings.Split(string(input), "\n")

	for i, line := range lines {
		// the version.txt and the conf.properties, must bear the same version, only then the version shld be changed or else error shld be logged
		// conf_version := strings.Contains(line, "weasis.version="+med_latest_patch_version+"-"+localversion)
		// if localversion == conf_version {
		if strings.Contains(line, "weasis.version="+med_latest_patch_version+"-"+localversion) {
			lines[i] = ("weasis.version=" + med_latest_patch_version + "-" + latest_patch_version)
		}
		// }
	}
	output := strings.Join(lines, "\n")
	err = ioutil.WriteFile(conf_file, []byte(output), 0644)
	if err != nil {
		log.Fatalln(err)
	}
}

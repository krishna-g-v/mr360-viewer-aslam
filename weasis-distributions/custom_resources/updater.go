// version - 1.2
// conditions -
// 1. URL on the server
// 2. Version info page on the server

// code conditions -
// 	1. URL: "http://viewer-updater.medreport360.com"
// 	2. VERSIONURL: "http://viewer-updater.medreport360.com/version_info.php"
// 	3. it works only if installed to C:/Program Files/MedReport360-Viewer

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
var revert_back1 bool

var URL = "http://viewer-updater.medreport360.com"                         // patch_files_url
var URLVERSION = "http://viewer-updater.medreport360.com/version_info.php" // version_info_url

// var URL = "http://localhost/demo" // patch_files_url
// var URLVERSION = "http://localhost/demo/version_info.php"

func init() {
	file, err := os.OpenFile(LOGFILE+"/logs.txt", os.O_APPEND|os.O_WRONLY, 0666)
	// file.WriteString(fmt.Sprintf("\n\n\n"))
	if err != nil {
		// ErrorLogger.Println("log file append error")
		// fmt.Println("log file append error")
	}

	//creating the log file
	_, err = os.Stat(LOGFILE + "/" + "logs.txt")
	// fmt.Println("Is logs.txt present in the directory", status)

	// check if logs.txt exist, if not create a new one"
	if os.IsNotExist(err) {
		os.Create(LOGFILE + "/" + "logs.txt")
		file.WriteString(fmt.Sprintf("\n\n\n"))
		// file.WriteString(fmt.Sprintf("===================The log file was not not found in the DIR: %d===================\n", LOGFILE))
		file.WriteString(fmt.Sprintf("================================Creating a new log file===================\n"))
		// ------------------------------if possible try to append the text to the existingg file
	} else {
		// file.WriteString(fmt.Sprintf("\n\n\n"))
		// file.WriteString(fmt.Sprintf("==========================Prev Log found, appending to the same===================\n"))
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

func main() {
	// checking on the server if any new version exists
	//parser, _ := http.NewRequest("GET", URL, nil)
	// filename := path.Base(parser.URL.Path)
	// InfoLogger.Println(fmt.Println("file name parsed from the URL: ", filename))

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
	// (?m)(?P<name>VERSION):\\s(?P<value>\\w+\\.?\\d*\\.?\\d*)
	medversion := regexp.MustCompile("(?m)(?P<name>VERSION):\\s(?P<value>\\d\\.\\d\\.\\d)") // working extractable build number
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
	// (?P<name>BUILD-VERSION): +?(?P<value>.?.?)
	re := regexp.MustCompile("(?m)(?P<name>BUILD-VERSION): +?(?P<value>.?.?)") // working extractable build number
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

	// BUILD-NO extraction \\ (?P<name>BUILD-NO): +?(?P<value>\d+)
	rc := regexp.MustCompile("(?m)(?P<name>BUILD-NO): +?(?P<value>\\d+)") // working extractable build number
	template = "$key=$value\n"
	buildnoresult := []byte{}
	for _, submatches := range rc.FindAllStringSubmatchIndex(body, -1) {
		buildnoresult = rc.ExpandString(buildnoresult, template, body, submatches)
		// fmt.Println(submatches)
	}
	buildno_version := string(buildnoresult)                              // converting the byte to a string
	buildno_latest_patch_version := strings.Trim(buildno_version, "= \n") // removign the character "="
	// fmt.Println(buildno_latest_patch_version)

	// gettting the present local version number
	createversion()
	local_content, err := ioutil.ReadFile(VERSIONFILE + "/version.txt")
	// if err != nil {
	// 	ErrorLogger.Println("The version.txt could not be found")
	// 	log.Fatal(err)
	// }
	// localversion := string(local_content)

	// getting the version
	getversion := regexp.MustCompile("(?m)(?P<name>version): (?P<value>\\d+)") // working extractable build number
	versionresult := []byte{}
	for _, submatches := range getversion.FindAllStringSubmatchIndex(string(local_content), -1) {
		versionresult = getversion.ExpandString(versionresult, template, string(local_content), submatches)
	}
	version_string := string(versionresult) // converting the byte to a string
	localversion := string(strings.Trim(version_string, "= \n"))

	// getting the build-no
	getbuild := regexp.MustCompile("(?m)(?P<name>BUILD-NO): (?P<value>\\d+)") // working extractable build number
	buildresult := []byte{}
	for _, submatches := range getbuild.FindAllStringSubmatchIndex(string(local_content), -1) {
		buildresult = getbuild.ExpandString(buildresult, template, string(local_content), submatches)
	}
	build_version_string := string(buildresult)                               // converting the byte to a string
	build_local_version := string(strings.Trim(build_version_string, "= \n")) // the build version specified local folders

	// fmt.Println(localversion)
	//done
	if revert_back1 && buildno_latest_patch_version < build_local_version {

		InfoLogger.Println("\n\n\nRevert_back specified as TRUE, reverting to version: ", latest_patch_version)
		delete_temp_files(latest_patch_version, build_local_version)
		// revert_back(latest_patch_version, med_latest_patch_version, localversion)
		patcher(latest_patch_version, localversion, buildno_latest_patch_version)
		InfoLogger.Println("Revert_back successfull, terminating the script")
		changeversiontxt(latest_patch_version, buildno_latest_patch_version)
		// conf_rename(med_latest_patch_version, latest_patch_version, localversion, buildno_latest_patch_version)
		InfoLogger.Println("updated the version.txt and conf.properties file")
		old_logs()
		os.Exit(0)

	} else if latest_patch_version > localversion || buildno_latest_patch_version > build_local_version {
		// InfoLogger.Println(fmt.Sprintf("found New patch (%s) for MedReport360-Viewer v%s", latest_patch_version, med_latest_patch_version))
		// if buildno_latest_patch_version > build_local_version {
		fmt.Println("\n\n\n")
		delete_temp_files(latest_patch_version, build_local_version)
		patcher(latest_patch_version, localversion, buildno_latest_patch_version)
		SuccessLogger.Println(fmt.Sprintf("successfully patched to the latest version %s for MedReport360-Viewer v%s", latest_patch_version, med_latest_patch_version))
		changeversiontxt(latest_patch_version, buildno_latest_patch_version)
		// conf_rename(med_latest_patch_version, latest_patch_version, localversion, buildno_latest_patch_version)
		InfoLogger.Println("updated the version.txt and conf.properties file")
		InfoLogger.Println("terminating the script")

		old_logs()
		// }
		os.Exit(0)
	} else {
		// // disabling the logger when there is no patch available
		// InfoLogger.Println(fmt.Sprintf("Already the latest patch %s for MedReport360-Viewer v%s running", localversion, med_latest_patch_version))
		// InfoLogger.Println(fmt.Sprintf("Revert_back specified as: %v, terminating the script here", revert_back1))
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

// can be removed in further releases, as both had the same functionality revert and patcher, this will no longer be required
// earlier used for deleting the files in prgram files/medreport, -- logic was incorrect
// func revert_back(latest_patch_version string, med_latest_patch_version string, localversion string, buildno_latest_patch_version string) {
// 	// here when reverting to previous version, the prev version zip should be available, how will the URL of the zip be specified,
// 	// or if checking from a list of files on the server?
// 	// fmt.Println("reverting back")
// 	// 1. patch shld download to the temp fiolder and extract there
// 	// 2. should run the script from the patch as admin
// 	// 3. modify the script file

// 	// file, _ := os.OpenFile(LOGFILE+"/"+"logs.txt", os.O_APPEND|os.O_WRONLY, 0666)
// 	// file.WriteString(fmt.Sprintf("\n========================================Reverting to the Previous Version============================="))
// 	// file.WriteString(fmt.Sprintf("\n==========================================Base Version %s================================", localversion))
// 	// file.WriteString(fmt.Sprintf("\n==================================The Revert_back version is %s================================", latest_patch_version))
// 	// file.WriteString(fmt.Sprintf("\n==========================The INSTALLDIR is: %s=======================", INSTALLDIR))
// 	// file.WriteString(fmt.Sprintf("\n====================The URL is: %s=====================\n", URL))

// 	// WarningLogger.Println("Deleting all the files in ", INSTALLDIR)
// 	// prevversion_filepath := (INSTALLDIR)
// 	// os.RemoveAll(prevversion_filepath + "/" + "app")
// 	// add a random code to check if deleted or not || osremoveall doesnt accept a value
// 	// if err != nil {
// 	// 	ErrorLogger.Println("error in deleting all the files in : ", prevversion_filepath)
// 	// 	log.Fatalln(err)
// 	// }
// 	// SuccessLogger.Println("successfully deleted all the files in : ", prevversion_filepath+"/"+"app")
// 	InfoLogger.Println("\n\n\nstarting the patching func")
// 	patch_url := (URL + "/patch_" + latest_patch_version + "_" + buildno_latest_patch_version + ".zip")
// 	// fmt.Println(patch_url)
// 	patching(patch_url)
// 	// deleting the patch file
// 	localfilepath := (Temp + "/patch_" + latest_patch_version + "_" + buildno_latest_patch_version + ".zip")
// 	os.RemoveAll(localfilepath)
// 	// deleting the patch folder
// 	tempfolderpath := (Temp + "/" + "viewer_64_patch")
// 	os.RemoveAll(tempfolderpath)
// 	// SuccessLogger.Println("successfully removed the patchfile: ", localfilepath)
// 	// SuccessLogger.Println("patching func was success")

// }

func patcher(latest_patch_version string, localversion string, buildno_latest_patch_version string) {
	// file, _ := os.OpenFile(LOGFILE+"/"+"logs.txt", os.O_APPEND|os.O_WRONLY, 0666)
	// file.WriteString(fmt.Sprintf("\n========================================patching the installer============================="))
	// file.WriteString(fmt.Sprintf("\n==========================================Base Version %s================================", localversion))
	// file.WriteString(fmt.Sprintf("\n==========================The INSTALLDIR is: %s=======================", INSTALLDIR))
	// file.WriteString(fmt.Sprintf("\n====================The URL is: %s=====================\n", URL))

	InfoLogger.Println("starting the patching func")
	patch_url := (URL + "/patch_" + latest_patch_version + "_" + buildno_latest_patch_version + ".zip")
	patching(patch_url)
	// deleting the patch file
	localfilepath := (Temp + "/patch_" + latest_patch_version + "_" + buildno_latest_patch_version + ".zip")
	os.RemoveAll(localfilepath)
	// deleting the patch folder
	tempfilepath := (Temp + "/" + "viewer_64_patch")

	os.RemoveAll(tempfilepath)
	// SuccessLogger.Println("successfully removed the patchfile: ", localfilepath)
	// SuccessLogger.Println("patching func was success")

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
	// InfoLogger.Println("Successfully created a local file to write in :", out)

	// checking the URL for access or active
	resp, err := http.Get(patch_url)
	if err != nil {
		ErrorLogger.Println("The URL is not accessible, URL:", patch_url)
		log.Fatalln(err)
	}
	// -------------------------------the file on wrong url is downloading some random corrupt file, and not showing the error
	defer resp.Body.Close()
	// InfoLogger.Println("URl is accessible, URL:", patch_url)

	// writing/downloading to the file
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

	//running the patch.bat file inside the script
	cmd := exec.Command("cmd", "/C", Temp+"/viewer_64_patch/patch.bat")
	cmd.Env = os.Environ()
	err = cmd.Run()
	// fmt.Printf("The o/p is %s\n", cmd)

	if err != nil {
		ErrorLogger.Println("Error: ", err)
		log.Fatal(err)
	}
	// SuccessLogger.Println("successfully removed the patchfolder: ", localfilepath)
	// ErrorLogger.Println("could not remove the patch file: ", localfilepath)
	// log.Fatalln(err)
}
func createversion() {
	// will create a version.txt in the [INSTALLDIR]/app with the version available on the system

	// will check if both the version and build-no exists in version.txt if not will delete the whole file, reason: may end up as glitch so resolving here
	file, _ := os.ReadFile(VERSIONFILE + "/version.txt")
	if strings.Contains(string(file), "version:") && (strings.Contains(string(file), "BUILD-NO:")) {

	} else {
		os.Remove(VERSIONFILE + "/version.txt")
	}

	_, err := os.OpenFile(VERSIONFILE+"/version.txt", os.O_APPEND|os.O_WRONLY, 0666)

	if os.IsNotExist(err) {
		// commenting as of now since it is not required
		ErrorLogger.Println("Version.txt not available, creating a new file")
		conf_file := INSTALLDIR + "/app/conf/config.properties"
		input, _ := ioutil.ReadFile(conf_file)
		lines := strings.Split(string(input), "\n")

		for i, _ := range lines { //(i < 174 && i > 160)
			confversion := regexp.MustCompile("(?m)(?P<name>weasis.version)=\\.?\\d*\\.?\\d*\\.\\d-(?P<value>\\d\\d)") // working extractable build number
			template := "$key=$value\n"
			confresult := []byte{}
			for _, submatches := range confversion.FindAllStringSubmatchIndex(lines[i], -1) {
				confresult = confversion.ExpandString(confresult, template, lines[i], submatches)
				// conf_version_string := string(conf_result)
			}
			conf_version_string := string(confresult) // converting the byte to a string
			conf_version := strings.Trim(conf_version_string, "= \n")
			write, _ := os.OpenFile(VERSIONFILE+"/version.txt", os.O_CREATE|os.O_WRONLY, 0666)
			write.WriteString("version: " + conf_version)
		}

		// fmt.Println(conf_version)

		for i, _ := range lines { //(i < 174 && i > 160)
			buildversion := regexp.MustCompile("(?m)(?P<name>BUILD-NO)=(?P<value>\\d+)") // working extractable build number
			template := "$key=$value\n"
			buildresult := []byte{}
			for _, submatches := range buildversion.FindAllStringSubmatchIndex(lines[i], -1) {
				buildresult = buildversion.ExpandString(buildresult, template, lines[i], submatches)
				// conf_version_string := string(conf_result)
			}
			build_version_string := string(buildresult) // converting the byte to a string
			build_version := strings.Trim(build_version_string, "= \n")
			// fmt.Println(conf_version)
			if build_version != "" {
				write, _ := os.OpenFile(VERSIONFILE+"/version.txt", os.O_APPEND|os.O_RDWR, 0666)
				write.WriteString("\nBUILD-NO: " + build_version)
			}
		}

	}
}
func changeversiontxt(latest_patch_version string, buildno_latest_patch_version string) {
	// will read the version present on the local system and as well will change the file to the latest version patched
	file, err := os.OpenFile(VERSIONFILE+"/version.txt", os.O_CREATE|os.O_WRONLY, 0666)
	if err != nil {
		ErrorLogger.Println("failed to read&change the version.txt in DIR", VERSIONFILE+"/version.txt")
	}
	file.WriteString("version: " + latest_patch_version + "\nBUILD-NO: " + buildno_latest_patch_version)
	// SuccessLogger.Println("updated version.txt to: ", latest_patch_version)
}

// working - will replace weasis.version=  & BUILD-NO= in .../app/conf/config.properties based on the latest patch version specified,
// will execute at last
// func conf_rename(med_latest_patch_version string, latest_patch_version string, localversion string, buildno_latest_patch_version string) {
// 	// to change the version inside conf/conf.properties automatically on latest patch
// 	// 1. post the patch, this part will find the weasis.properties in conf/config.properties
// 	// 2. it will replace the version there with our latest patch as done
// 	// 3. ex: weasis.version=3.8.1-32  BUILD-NO=1001

// 	// commenting as of now since it is not required
// 	conf_file := INSTALLDIR + "/app/conf/config.properties"
// 	input, _ := ioutil.ReadFile(conf_file)
// 	lines := strings.Split(string(input), "\n")

// 	for i, line := range lines { //(i < 174 && i > 160)
// 		confversion := regexp.MustCompile("(?m)(?P<name>weasis.version)=\\.?\\d*\\.?\\d*\\.\\d-(?P<value>\\d\\d)") // working extractable build number
// 		template := "$key=$value\n"
// 		confresult := []byte{}
// 		for _, submatches := range confversion.FindAllStringSubmatchIndex(lines[i], -1) {
// 			confresult = confversion.ExpandString(confresult, template, lines[i], submatches)

// 		}
// 		conf_version_string := string(confresult) // converting the byte to a string
// 		conf_version := strings.Trim(conf_version_string, "= \n")

// 		if strings.Contains(line, "weasis.version="+med_latest_patch_version+"-"+conf_version) {
// 			lines[i] = ("weasis.version=" + med_latest_patch_version + "-" + latest_patch_version)
// 		} else {
// 			ErrorLogger.Println("could not replace the weasis.version=  & BUILD-NO= in .../app/conf/config.properties")
// 		}

// 		//similarly checking the build version and replacing it with the latest version obtained
// 		buildversion := regexp.MustCompile("(?m)(?P<name>BUILD-NO)=(?P<value>\\d+)") // working extractable build number
// 		template1 := "$key=$value\n"
// 		buildresult := []byte{}
// 		for _, submatches := range buildversion.FindAllStringSubmatchIndex(lines[i], -1) {
// 			buildresult = buildversion.ExpandString(buildresult, template1, lines[i], submatches)

// 		}
// 		build_version_string := string(buildresult) // converting the byte to a string
// 		build_version := strings.Trim(build_version_string, "= \n")

// 		if strings.Contains(line, "BUILD-NO="+build_version) {
// 			lines[i] = ("BUILD-NO=" + buildno_latest_patch_version)
// 		} else {

// 			ErrorLogger.Println("could not replace the weasis.version=  & BUILD-NO= in .../app/conf/config.properties")
// 		}

// 	}

// 	output := strings.Join(lines, "\n")
// 	InfoLogger.Println("updated the weasis.version=  & BUILD-NO= in .../app/conf/config.properties")
// 	err := ioutil.WriteFile(conf_file, []byte(output), 0644)
// 	if err != nil {
// 		ErrorLogger.Println("could not write to file .../app/conf/config.properties for weasis.version=  & BUILD-NO=")
// 	}
// }

func delete_temp_files(latest_patch_version string, build_local_version string) {
	// will delete all the files related to our patching, prior starting the patching
	temp_patch_file := Temp + "/patch_" + latest_patch_version + "_" + build_local_version + ".zip"
	temp_patch_folder := Temp + "\\" + "viewer_64_patch"
	tasks := []string{temp_patch_file, temp_patch_folder}
	for _, s := range tasks {
		_, err := os.Stat(s)
		// fmt.Println("Is logs.txt present in the directory", status)
		// check if logs.txt exist, if not create a new one"
		if os.IsNotExist(err) {
			continue
		} else {
			os.RemoveAll(s)
		}

	}
}

func old_logs() {
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

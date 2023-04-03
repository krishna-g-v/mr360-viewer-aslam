# MedReport360Viewer

# source /d/work/javawork/setenv.sh
# make sure that you use jdk-18.0.1.1 and maven-3.8.4+



# For Portable Version
mvn clean install
mvn clean package -Dportable=true -P compressXZ -f weasis-distributions



# For MSI Installer
mvn clean install -Dchangelist=
mvn clean package -Dchangelist= -Dportable=true -P compressXZ -f weasis-distributions

cd weasis-distributions/script/

./package-weasis.sh --input /d/work/javawork/medreport360-viewer/weasis-distributions/target/portable-dist/ --output /d/work/javawork/medreport360-viewer/weasis-distributions/target/native-inst/ --jdk /d/work/java/jdk-18.0.1.1/


# To build updater.go for windows x86_64
env GOOS=windows GOARCH=amd64 go build updater.go


# jcef inclusion
# from medreport360-viewer/weasis-core/weasis-core-ui folder run the following with having jcef.jar packaged from extracting all the files from all the jars
cd weasis-core/weasis-core-ui/
jar -cvf ./jcef_lib/jcef.jar *
mvn install:install-file -Dfile=./jcef_lib/jcef.jar -DgroupId=com.mr360 -DartifactId=jcef -Dpackaging=jar -Dversion=1.0
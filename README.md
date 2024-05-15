# MedReport360Viewer

# source /d/tmp/setenv.sh
# make sure that you use jdk-18.0.1.1 and maven-3.8.4+



# For Portable Version
mvn clean install
mvn clean package -Dportable=true -P compressXZ -f weasis-distributions



# For MSI Installer
mvn clean install -Dchangelist=
mvn clean package -Dchangelist= -Dportable=true -P compressXZ -f weasis-distributions

cd weasis-distributions/script/

./package-weasis.sh --input /d/tmp/medreport360-viewer/weasis-distributions/target/portable-dist/ --output /d/tmp/medreport360-viewer/weasis-distributions/target/native-inst/ --jdk /d/work/java/jdk-18.0.1.1/


# To build updater.go for windows x86_64
env GOOS=windows GOARCH=amd64 go build updater.go


# To resize the icons in bulk
https://redketchup.io/bulk-image-resizer


# known issues
1. issue in task scheduler - should come with admin priviliges
2. issue in task scheduler, by the time the task is getting created the trigger time is getting elapsed
3. should add some extra conditions in wix to remove AC adapter, trigger on fail 


# to enable debugger on installed app
[JavaOptions]
java-options=-Xdebug
java-options=-Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n
java-options=-Djpackage.app-version=3.8.1
java-options=-splash:$APPDIR\resources\images\about-round.png
java-options=-Dgosh.port=17179
java-options=--add-exports=java.base/sun.net.www.protocol.http=ALL-UNNAMED
java-options=--add-exports=java.base/sun.net.www.protocol.file=ALL-UNNAMED
java-options=--add-exports=java.base/sun.net.www.protocol.https=ALL-UNNAMED
java-options=--add-exports=java.base/sun.net.www.protocol.ftp=ALL-UNNAMED
java-options=--add-exports=java.base/sun.net.www.protocol.jar=ALL-UNNAMED
java-options=--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED
java-options=--add-opens=java.base/java.net=ALL-UNNAMED
java-options=--add-opens=java.base/java.lang=ALL-UNNAMED
java-options=--add-opens=java.base/java.security=ALL-UNNAMED
java-options=--add-opens=java.base/java.io=ALL-UNNAMED
java-options=--add-opens=java.desktop/javax.imageio.stream=ALL-UNNAMED
java-options=--add-opens=java.desktop/javax.imageio=ALL-UNNAMED
java-options=--add-opens=java.desktop/com.sun.awt=ALL-UNNAMED

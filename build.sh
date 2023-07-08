#!/bin/sh

NAME=fluent
CLASSNAME=Fluent
VERSION=0.1.0
TARGET=9
JAR=$NAME.jar

# location of jdk for building
[ ! "$JAVA_HOME" ] && JAVA_HOME="$(dirname $(dirname $(readlink -f $(which javac))))"

# directories containing jdks to test against, separated by spaces
JDKS="$JAVA_HOME"
JDKS="$HOME/tools/jdk-*"

# javac arguments to invoke the compiled plugin
WITH_PLUGINS="-Xplugin:$NAME -J--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED"
TEST_CLASSPATH=$JAR

# compile and build jar
# note: -source 8 is required to import com.sun.tools.javac.*
echo "===== BUILDING ====="
echo $JAVA_HOME
[ -d target ] && rm -r target
mkdir -p target/META-INF/services
echo "com.sun.tools.javac.comp.$CLASSNAME" > target/META-INF/services/com.sun.source.util.Plugin
$JAVA_HOME/bin/javac -nowarn -source 8 -target $TARGET -d target $CLASSNAME.java
[ $? -eq 0 ] || exit 1
cd target; $JAVA_HOME/bin/jar --create --file ../$JAR *; cd ..

# test against all jdks
echo "\n===== TESTING ====="
for JDK in $JDKS; do
    echo $JDK
    "$JDK"/bin/javac -cp $TEST_CLASSPATH -d target $WITH_PLUGINS TestValid.java
    [ $? -eq 0 ] || exit 1
    "$JDK"/bin/java -cp target -enableassertions TestValid
    [ $? -eq 0 ] || exit 1
done
echo "\n----- press enter to begin error test cases"; read x
for JDK in $JDKS; do
    echo $JDK
    "$JDK"/bin/javac -cp $TEST_CLASSPATH -d target $WITH_PLUGINS TestErrors.java
    echo "\n----- press enter to continue"; read x
done

# install using maven
echo "===== INSTALLING WITH MAVEN ====="
mvn install:install-file -DgroupId=jamaica -DartifactId=$NAME -Dversion=$VERSION -Dpackaging=jar -Dfile=$JAR


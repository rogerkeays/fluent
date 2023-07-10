#!/bin/sh

# build configuration
GROUP=jamaica
NAME=fluent
VERSION=0.2.1
TARGET=9

# use sun package to override package methods
PACKAGE=com.sun.tools.javac.comp
CLASSNAME=Fluent
JAR=$NAME.jar
PLUGIN=-Xplugin:fluent
TEST_OPTS=-J--add-opens=java.base/java.lang=ALL-UNNAMED
TEST_CLASSPATH=$JAR

# location of jdk for building
[ ! "$JAVA_HOME" ] && JAVA_HOME="$(dirname $(dirname $(readlink -f $(which javac))))"

# directories containing jdks to test against, separated by spaces
JDKS="$JAVA_HOME"
[ "$1" = "all" ] && JDKS="$HOME/tools/jdk-*"

# compile and build jar
# note: -source 8 is required to import com.sun.tools.javac.*
echo "===== BUILDING ====="
echo $JAVA_HOME
[ -d target ] && rm -r target
mkdir -p target/META-INF/services
echo "$PACKAGE.$CLASSNAME" > target/META-INF/services/com.sun.source.util.Plugin
$JAVA_HOME/bin/javac -Xlint:unchecked -nowarn -source 8 -target $TARGET -d target $CLASSNAME*.java
[ $? -eq 0 ] || exit 1
cd target; $JAVA_HOME/bin/jar --create --file ../$JAR *; cd ..

# test against all jdks
echo "\n===== TESTING ====="
echo "----- press enter to run valid test cases"; read x
for JDK in $JDKS; do
    echo $JDK
    "$JDK"/bin/javac -cp $TEST_CLASSPATH -d target "$PLUGIN" $TEST_OPTS TestValid.java
    [ $? -eq 0 ] || exit 1
    "$JDK"/bin/java -cp target -enableassertions TestValid
    [ $? -eq 0 ] || exit 1
done
echo "\n----- press enter to run error test cases"; read x
for JDK in $JDKS; do
    echo $JDK
    "$JDK"/bin/javac -cp $TEST_CLASSPATH -d target "$PLUGIN" $TEST_OPTS TestErrors.java
done

# install using maven
echo "\n===== INSTALLING WITH MAVEN ====="
echo "----- press enter to install using maven"; read x
mvn install:install-file -DgroupId=$GROUP -DartifactId=$NAME -Dversion=$VERSION -Dpackaging=jar -Dfile=$JAR


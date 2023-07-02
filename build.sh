#!/bin/sh

VERSION=0.1.0

# location of jdk for building fluent
[ ! "$JAVA_HOME" ] && JAVA_HOME="$(dirname $(dirname $(readlink -f $(which javac))))"

# directories containing jdks to test against, separated by spaces
JDKS="$HOME/tools/jdk-*"
[ ! "$JDKS" ] && JDKS="$JAVA_HOME"

# target java 8 so we can import com.sun.tools.javac.*
TARGET=8

# compile and build jar
echo "===== BUILDING ====="
echo $JAVA_HOME
[ -d target ] && rm -r target
mkdir -p target/META-INF/services
echo "com.sun.tools.javac.comp.Fluent" > target/META-INF/services/com.sun.source.util.Plugin
$JAVA_HOME/bin/javac -source $TARGET -target $TARGET -d target Fluent.java
cd target; $JAVA_HOME/bin/jar --create --file ../fluent.jar *; cd ..

# test against all jdks
echo "\n===== TESTING ====="
echo "----- press enter to being testing valid code"; read x
for JDK in $JDKS; do
    echo $JDK
    "$JDK"/bin/javac -cp fluent.jar -Xplugin:fluent -d target TestValid.java
    "$JDK"/bin/java -cp target -enableassertions TestValid
done
echo "\n----- press enter to begin testing code with errors"; read x
for JDK in $JDKS; do
    echo $JDK
    "$JDK"/bin/javac -cp fluent.jar -Xplugin:fluent -d target TestErrors.java
    echo "\n----- press enter to continue"; read x
done

# install using maven
echo "===== INSTALLING WITH MAVEN ====="
mvn install:install-file -DgroupId=jamaica -DartifactId=fluent -Dversion=$VERSION -Dpackaging=jar -Dfile=fluent.jar


#!/bin/sh

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
echo "===== TESTING ====="
for JDK in $JDKS; do
    echo $JDK
    "$JDK"/bin/javac -cp fluent.jar -Xplugin:fluent -d target test.java
    "$JDK"/bin/java -cp target -enableassertions test
done


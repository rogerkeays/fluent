#!/bin/sh

# expose the compiler module by targetting java 8
TARGET=8

# compile and build jar
echo "BUILDING"
[ -d target ] && rm -r target
mkdir -p target/META-INF/services
echo "com.sun.tools.javac.comp.Fluent" > target/META-INF/services/com.sun.source.util.Plugin
javac -source $TARGET -target $TARGET -d target Fluent.java
cd target; jar --create --file fluent.jar *
cd ..

# test
echo "TESTING"
javac -cp target/fluent.jar -Xplugin:fluent test.java
java test


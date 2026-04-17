#!/bin/bash
cd "$(dirname "$0")"
mkdir -p out
find src/jdm -name "*.java" | xargs javac -cp "lib/*" -d out
java -cp "out:lib/*" jdm.Main
read 
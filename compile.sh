#!/bin/sh
# Compile all sources into ./out
mkdir -p out
javac -d out $(find src -name "*.java") && echo "Compiled successfully."

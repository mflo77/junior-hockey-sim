#!/bin/bash

mkdir -p out
find src -name "*.java" > sources.txt
javac -source 17 -target 17 -d out @sources.txt && echo "✅ Compiled successfully!"
rm sources.txt

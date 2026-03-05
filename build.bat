@echo off
mkdir out 2>nul
dir /s /b src\*.java > sources.txt
javac -source 17 -target 17 -d out @sources.txt && echo Compiled successfully!
del sources.txt

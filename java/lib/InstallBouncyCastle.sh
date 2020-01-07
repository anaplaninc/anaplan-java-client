#!/usr/bin/env bash
#This script install Bouncy Castle jar in the jdk folder
#It first determines the JAVA_HOME and appends /jre/lib/ext where the bouncy castle gets installed
#In case of Success or Failure it will print appropriate message on console

echo "Installing Bouncy Castle jar......"
path=${JAVA_HOME}/jre/lib/ext
echo $path
filename="bcprov-jdk15on-164.jar"
echo $filename
sudo cp "$filename" "$path"
if [ $? -eq 0 ]
then
    echo "Installed Bouncy Castle jar Successfully!!!"
else
    echo "There is an error installing Bouncy Castle jar"
fi

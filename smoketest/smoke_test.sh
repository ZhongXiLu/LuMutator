#!/bin/bash

# Assume LuMutator is already packaged in jar file
cp target/lumutator-1.0-SNAPSHOT.jar smoketest/
cd smoketest

# Download the project if necessary
if [ ! -d JPacman ]
then
  curl -O https://ansymore.uantwerpen.be/system/files/uploads/courses/Testing/JPacman.zip
  unzip JPacman.zip
fi

# Set up the required files
cd JPacman
mvn clean
cp ../pom.xml ./
cp ../config.xml ./
mv ../lumutator-1.0-SNAPSHOT.jar ./

# Set the classpath in the config.xml
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
sed -i 's/[\/&]/\\&/g' cp.txt   # Escape characters
sed -i "s/<classPath\/>/<classPath>$(cat cp.txt):target\/classes\/:target\/test-classes\/<\/classPath>/g" config.xml

# Mutation analysis using PITest
mvn test -Dfeatures=+EXPORT org.pitest:pitest-maven:mutationCoverage

# Run LuMutator
java -jar lumutator-1.0-SNAPSHOT.jar -c config.xml

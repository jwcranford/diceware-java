# diceware-java

A Java command-line tool to generate passphrases based on the diceware
method described at
http://world.std.com/~reinhold/dicewarefaq.html#computer.

## To build

Gradle and Java 8 are required to build diceware-java.

$ gradle build

## To run

$ java -jar build/libs/diceware-java-0.1.jar --help

## To install

Extract either the tar or zip distribution in build/distributions,
then run the following from the extracted directory:

$ bin/diceware-java --help

Note that both a shell script and a batch file is available in the bin
directory.

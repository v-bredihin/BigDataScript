#!/bin/sh -e

grammar="BigDataScript.g4"
init="main"
testFile="../test/z.bds"

# Programs
jar="../lib/antlr-4.2.2-complete.jar"
antlr4="java -Xmx1g -cp $jar org.antlr.v4.Tool"
grun="java -Xmx1g -cp .:$jar org.antlr.v4.runtime.misc.TestRig"

# Delete old files
touch tmp.java tmp.class
rm *.class *.java 

# Compile
echo Create Lexer and Parser
$antlr4 -visitor -package ca.mcgill.mcb.pcingola.bigDataScript.antlr $grammar

# Copy to source dir
cp *.java ../src/ca/mcgill/mcb/pcingola/bigDataScript/antlr/


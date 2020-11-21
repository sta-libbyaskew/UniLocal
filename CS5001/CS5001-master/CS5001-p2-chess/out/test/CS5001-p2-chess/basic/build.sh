#!/bin/bash
JUNITPATH="$TESTDIR"
FILES=$(find . -name '*.java')

javac -cp "$JUNITPATH/junit.jar":"$JUNITPATH/hamcrest.jar":. $FILES

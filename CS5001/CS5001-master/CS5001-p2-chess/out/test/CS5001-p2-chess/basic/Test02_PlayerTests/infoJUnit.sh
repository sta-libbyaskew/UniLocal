#!/bin/bash
JUNITPATH="$TESTDIR"
TESTPACKAGE="player_tests"
TESTRUNNER="PlayerTestSuiteRunner"

if [ -d "../p2-animal-chess-basic-tests/$TESTPACKAGE" ]; then
	echo "ERROR: directoy ../p2-animal-chess-basic-tests/$TESTPACKAGE already exists. Please remove or rename and re-run stacscheck"
	exit 1
else 
	mkdir -p ../p2-animal-chess-basic-tests/$TESTPACKAGE
	cp "$JUNITPATH"/$TESTPACKAGE/*.java ../p2-animal-chess-basic-tests/$TESTPACKAGE/

	javac -cp "$JUNITPATH/junit.jar":"$JUNITPATH/hamcrest.jar":. ../p2-animal-chess-basic-tests/$TESTPACKAGE/*.java

	java -cp "$JUNITPATH/junit.jar":"$JUNITPATH/hamcrest.jar":../p2-animal-chess-basic-tests:.  $TESTPACKAGE.$TESTRUNNER
	
	rm -r ../p2-animal-chess-basic-tests/$TESTPACKAGE
fi


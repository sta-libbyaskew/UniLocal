#!/bin/bash
ROOT=$TESTDIR/../../../Resources/www
(timeout 2 java WebServerMain $ROOT 12345 > /dev/null 2>&1) & (sleep 1 ; curl -s localhost:12345/index.html)
wait

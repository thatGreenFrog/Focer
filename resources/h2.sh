#!/bin/sh
dir=$(dirname "$0")
java -cp "$dir/db/h2-1.4.200.jar:$H2DRIVERS:$CLASSPATH" org.h2.tools.Console "$@"

#!/bin/bash
# Startup script for Phex


# Determine the directory where this script exists.
# We assume that's the I2Phex install directory.

OWN_LOCATION=$(dirname $0)


# Adapt the variable below to pass various options to Java.
# We definitely want the current directory in library path,
# or we keep extracting a jbigi library from its .jar over and over
# instead of using a pre-extracted version.

JAVAOPTS="-Djava.library.path=."


# Call Java to run our .jar archive.
# The main class and classpath are established inside it by manifest attributes.
# That seems a good call, since a manifest ain't platform dependent.
# See the build file for details.

java ${JAVAOPTS} -jar "${OWN_LOCATION}/lib/phex.jar"

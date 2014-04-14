REM Startup script for I2Phex
REM Adapt the variable below to pass various options to Java.

REM We definitely want the current directory in library path,
REM or we keep extracting a jbigi library from its .jar over and over
REM instead of using a pre-extracted version.

SET JAVAOPTS="-Djava.library.path=."

REM Call Java to run our .jar archive.
REM The main class and classpath are established inside it by manifest attributes.
REM That seems a good call, since a manifest ain't platform dependent.
REM See the build file for details.

start javaw %JAVAOPTS% -jar lib\phex.jar

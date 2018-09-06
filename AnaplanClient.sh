#!/bin/sh
# Assumes this POSIX shell script resides in the same directory as
# anaplan-connect-X.jar, and lib directory containing any additional
# libraries to be added to the class path, eg: JDBC drivers, etc.

# Behavior:
# 1. If the Java binary path is hardcoded, then use it, ignore everything else (JAVA_HOME, etc.)
# 2. If Java binary is not hardcoded, then determine JAVA_HOME from environment variable if set,
#    or get the Java path by running 'which java'.
# 3. Build the Java binary path using the JAVA_HOME value, validate that 'JAVA_HOME\bin\java -fullversion' is the valid Java supported, which is Java8.

# Provide the absolute path to Java8 installation (optional)
# NOTE: If the customer requires Java_Home to be set to 1.6, then override this
# section with the directory for Java 8)
_java=

######################### Do not edit below this line #########################

# Fetch the Java version if set, else determine where its installed
if [ -z "$_java" ]; then
  echo "Absolute path to JAVA8 executable not provided. Will attempt to figure out path from JAVA_HOME or which command..."
  # Pick up first JAVA in the path, unless JAVA_HOME is set in environment
  if [ -n "${JAVA_HOME}" -a -e "${JAVA_HOME}/bin/java" ]; then
    _java=${JAVA_HOME}/bin/java
    echo "Found Java executable from JAVA_HOME..."
  elif ( which java >/dev/null ); then
    _java=`which java`
    echo "Found Java executable from 'which' command: $_java"
  else
    echo "ERROR: Cannot locate a Java runtime!" >&2
    exit 100
  fi
else
  echo "Java path defined: $_java"
fi

# Check if we have the correct version of Java (7 or 8)
if [[ "$_java" ]]; then
  java8=0
  if [ `${_java} -version 2>&1 | grep 1.8 | wc -l` -gt 0 ]; then
    echo "Java 8 found!"
    java8=1
  fi
  if [[ ${java8} == 0 ]]; then
    echo "ERROR: The Java version you have is not supported by Anaplan Connect. Please upgrade to Java8." >&2
    exit 100
  fi
fi

# Set up the classpath
here=`dirname "$0"`

# Pick up the most recent Anaplan Connect jar
classpath=`ls -t "${here}"/anaplan-connect-*.jar | head -1`
if [ ! -f "${classpath}" ]; then
  echo "Cannot locate anaplan-connect-@rel@.jar" >&2
  exit 1
elif [ "${here}/anaplan-connect-@rel@.jar" != "${classpath}" ]; then
  echo "Using ${classpath}" >&2
fi

# Check for /lib directory where all dependency JARs exist for Anaplan Connect
lib="${here}/lib"
if [ -d "${lib}" ]; then
  for l in "${lib}"/*; do
    classpath=${classpath}:${l}
  done
else
  echo "Warning: cannot find lib directory" >&2
fi

# Start the Java virtual machine
exec "${_java}" ${JAVA_OPTS} -classpath "${classpath}" com.anaplan.client.Program "$@"

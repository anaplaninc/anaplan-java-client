#!/bin/sh
# Assumes this POSIX shell script resides in the same directory as
# anaplan-connect-@rel@.jar, and lib directory containing any additional
# libraries to be added to the class path.
# Pick up first java in the path, unless JAVA_HOME is set in environment
if [ -n "${JAVA_HOME}" -a -e "${JAVA_HOME}/bin/java" ]; then
  java=${JAVA_HOME}/bin/java
elif ( which java >/dev/null ); then
  java=`which java`
else
  echo "Cannot locate a Java runtime; if Java is installed, please set JAVA_HOME to the location of your Java installation" >&2
  exit 1
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
lib="${here}/lib"
if [ -d "${lib}" ]; then
  for l in "${lib}"/*; do
    classpath=${classpath}:${l}
  done
else
  echo "Warning: cannot find lib directory" >&2
fi
# Start the Java virtual machine
#echo "${java}" ${JAVA_OPTS} -classpath "${classpath}" com.anaplan.client.Program "$@"
exec "${java}" ${JAVA_OPTS} -classpath "${classpath}" com.anaplan.client.Program "$@"

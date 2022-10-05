#!/bin/sh

PROJECT_VERSION=$(mvn -q \
    -Dexec.executable=echo \
    -Dexec.args='${project.version}' \
    --non-recursive \
    exec:exec)

FILE_NAME=sha-digest-version.txt
CURRENT_DATE_TIME=$(date '+%Y-%m-%dT%T.%zZ')
SHA_256_VALUE=$(cd anaplan-connect-client/target; openssl dgst -sha256 anaplan-connect-client-${PROJECT_VERSION}-jar-with-dependencies.jar | cut -d=  -f2)

RECORD="DATE ${CURRENT_DATE_TIME} | Version ${PROJECT_VERSION} | SHA digestID: ${SHA_256_VALUE}"
echo "$(echo ${RECORD}; cat ${FILE_NAME})" > $FILE_NAME

#!/bin/sh
VERSION="3.0.0"


FILE_NAME=sha-digest-version.txt
CURRENT_DATE_TIME=$(date '+%Y-%m-%dT%T.%zZ')
SHA_256_VALUE=$(cd anaplan-connect-client/target; openssl dgst -sha256 anaplan-connect-client-${VERSION}-jar-with-dependencies.jar | cut -d=  -f2)

RECORD="DATE ${CURRENT_DATE_TIME} | Version ${VERSION} | SHA digestID: ${SHA_256_VALUE}"
echo "$(echo ${RECORD}; cat ${FILE_NAME})" > $FILE_NAME

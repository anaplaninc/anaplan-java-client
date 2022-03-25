#!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh

AnaplanUser=$UserCredentials
ActionName="LargeRussianList from LargeRussianList.txt"
FilePath="data/LargeRussianList.txt"
ErrorDump="dump/LargeRussianList from LargeRussianList.txt"
FileName="LargeRussianList.txt"

Operation="-debug -file '${FileName}' -auth '${AuthUrl}' -chunksize 1 -put '${FilePath}' -import '${ActionName}' -execute -output '${ErrorDump}' "

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
#Command="cd .. && ./AnaplanClient.sh  ${Credentials} -workspace ${WorkspaceId} -model ${ModelId} ${Operation}"

Command="./AnaplanClient.sh  ${Credentials} -service ${ServiceUrl} -workspace ${WorkspaceId} -model ${ModelId} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

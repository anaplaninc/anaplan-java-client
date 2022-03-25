#!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh

AnaplanUser=$UserCredentials
ActionName="ChineseBig5 from ChineseBig5.txt"
FilePath="data/ChineseBig5.txt"
ErrorDump="dump/ChineseBig5 from ChineseBig5.txt"
FileName="ChineseBig5.txt"

Operation="-debug -file '${FileName}' -auth '${AuthUrl}' -chunksize 1 -put '${FilePath}' -import '${ActionName}' -execute -output '${ErrorDump}' "

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"

Command="./AnaplanClient.sh   ${Credentials} -service ${ServiceUrl} -workspace ${WorkspaceId} -model ${ModelId} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

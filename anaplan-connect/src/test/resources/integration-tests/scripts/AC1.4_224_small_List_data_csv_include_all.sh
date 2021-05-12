#!/bin/sh
# This example deletes selected content from the module
. ./config/config.sh
AnaplanUser=$UserCredentials
ImportName=""
ExportName=""
FileName=""
FilePath="list-data/list-items-includeall-small-list.csv"
ChunkSize=1
OutputName=""
ErrorDump=""
ListName="small list"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace '${WorkspaceName}' -model ${ModelId} -l '${ListName}' -execute:all -get:csv '${FilePath}'"

#____________________________ Do not edit below this line ______________________________

Credentials="-user ${UserCredentials}"

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
if [ ! -f AnaplanClient.sh ]; then
  echo "Please ensure this script is in the same directory as AnaplanClient.sh." >&2
  exit 1
elif [ ! -x AnaplanClient.sh ]; then
  echo "Please ensure you have executable permissions on AnaplanClient.sh." >&2
  exit 1
fi

Command="./AnaplanClient.sh ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

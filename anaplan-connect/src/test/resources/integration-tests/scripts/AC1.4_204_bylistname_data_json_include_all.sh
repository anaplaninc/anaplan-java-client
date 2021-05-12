#!/bin/sh
# This example deletes selected content from the module
. ./config/config.sh
AnaplanUser=$UserCredentials

ActionName=""
ImportName=""
ExportName=""
FileName=""
FilePath="list-data/list-items-byname-includeall.json"
ChunkSize=1
OutputName=""
ErrorDump=""
ListId="Data from Gaga"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId} -list '${ListId}' -execute:all -get:json '${FilePath}'"

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

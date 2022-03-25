#!/bin/sh
# This example deletes selected content from the module
. ./config/config.sh
AnaplanUser=$UserCredentials
ActionName="cyrillicDelete"
ImportName=""
ExportName=""
FileName=""
FilePath=""
ChunkSize=""
OutputName=""

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -model"
#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

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

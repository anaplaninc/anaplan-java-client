#!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh
AnaplanUser=$UserCredentials

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId} -export 'Grid - LargeListUTF16LE.txt' -execute -get 'export/imported-exported-LargeListUTF16LE.txt'"

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

#Command="./AnaplanClient.sh ${Credentials} -workspace ${WorkspaceId} -model ${ModelId} ${Operation}"

Command="./AnaplanClient.sh ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

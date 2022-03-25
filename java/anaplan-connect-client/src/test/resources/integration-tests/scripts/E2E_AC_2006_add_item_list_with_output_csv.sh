#!/bin/sh
# This example add items to list from csv file
. ./config/config.sh
FilePath="data/INTEGRA-3055-special.csv"
ListId="101000000138"
OutputLog="ItemsLog.txt"
WorkspaceId="\"Default Integrations Workspace\""
ModelId="72E11A73F55D41A98CCDF8626210B96E"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId} -l ${ListId} -execute -putItems:csv ${FilePath} -output:csv '${OutputLog}'"

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
#Command="./AnaplanClient.sh ${Credentials} -workspace ${WorkspaceId} -model ${ModelId} ${Operation}"
Command="./AnaplanClient.sh ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"
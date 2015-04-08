#!/bin/sh
# This example uploads a file and runs an import

AnaplanUser=fred.smith@mycompany.com
WorkspaceId="'My Workspace'"
ModelId="'My Model'"
Operation="-file 'My Source.txt' -put 'data/My Source.txt' -import 'My Module from My Source.txt' -execute -output 'data/My Errors.txt'"

#____________________________ Do not edit below this line ______________________________
if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

echo cd "`dirname "$0"`"
cd "`dirname "$0"`"
if [ ! -f AnaplanClient.sh ]; then
  echo "Please ensure this script is in the same directory as AnaplanClient.sh." >&2
  exit 1
elif [ ! -x AnaplanClient.sh ]; then
  echo "Please ensure you have executable permissions on AnaplanClient.sh." >&2
  exit 1
fi
Command="./AnaplanClient.sh ${Credentials} -workspace ${WorkspaceId} -model ${ModelId} ${Operation}"
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

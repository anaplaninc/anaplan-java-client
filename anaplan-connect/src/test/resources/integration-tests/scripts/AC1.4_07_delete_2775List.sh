#!/bin/sh
# This example deletes selected content from the module
. ./config/config.sh
AnaplanUser=$UserCredentials
ActionName="DeleteCoresrv2776List"
ImportName=""
ExportName=""
FileName=""
FilePath=""
ChunkSize=""
OutputName=""
ModelName="Anaplan Connect Testing Automation Model"
WorkspaceName="Default Integrations Workspace"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace '${WorkspaceName}' -model '${ModelName}' -action '${ActionName}' -execute"

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
#Command="cd .. && ./AnaplanClient.sh  ${Credentials} -workspace ${WorkspaceId} -model ${ModelId} ${Operation}"

Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

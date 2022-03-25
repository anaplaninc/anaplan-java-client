#!/bin/sh
# This example deletes selected content from the module
. ./config/config.sh
CertPath=$CACertPath
ActionName="Delete coresrv-2775-list"
ImportName=""
ExportName=""
FileName=""
FilePath=""
ChunkSize=""
OutputName=""

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId} -action '${ActionName}' -execute"

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi
if [ "${KeyStorePath}" ]; then
  Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"
  #Credentials="-certificate ${CertPath} -keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"    # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
  # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
#Command="cd .. && ./AnaplanClient.sh  ${Credentials} -workspace ${WorkspaceId} -model ${ModelId} ${Operation}"
Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

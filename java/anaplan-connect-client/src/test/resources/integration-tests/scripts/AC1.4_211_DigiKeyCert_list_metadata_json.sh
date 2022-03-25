#!/bin/sh
# This example list module list
. ./config/config.sh
AnaplanUser=$CACertPath
ActionName=""
ImportName=""
ExportName=""
FileName=""
FilePath="list-metadata/list-metadata.json"
ChunkSize=1
OutputName=""
ListId="101000000111"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId}  -list ${ListId} -get:json ${FilePath}"

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

if [ "${KeyStorePath}" ]; then
  #Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"
  Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}" # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
  # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

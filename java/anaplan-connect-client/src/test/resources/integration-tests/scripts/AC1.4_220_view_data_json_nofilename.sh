#!/bin/sh
# This example list module list
. ./config/config.sh
AnaplanUser=$UserCredentials
ActionName=""
ImportName=""
ExportName=""
FileName=""
ChunkSize=1
OutputName=""
ModuleId="102000000023"
ViewId="102000000023"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId} -module ${ModuleId} -view ${ViewId} -execute -get:json"

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

if [ "${CertPath}" ]; then
  #Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"
  Credentials="-certificate ${CertPath} -keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}" # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
  # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

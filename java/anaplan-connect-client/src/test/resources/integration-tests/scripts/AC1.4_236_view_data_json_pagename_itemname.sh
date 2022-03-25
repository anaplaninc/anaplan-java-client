#!/bin/sh
# This example list module list
. ./config/config.sh
AnaplanUser=$UserCredentials
ActionName=""
ImportName=""
ExportName=""
FileName=""
FilePath="view-data/view-data-paged.json"
ChunkSize=1
OutputName=""
ModuleId="view-data-test"
ViewId="default"
PageDimension="view-data-test-dimension:a"
workspace="Default Integrations Workspace"
model="Anaplan Connect Testing Automation Model"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${workspace} -model ${model} -module ${ModuleId} -view ${ViewId} -pages "view-data-test-dimension:a" -execute -get:json ${FilePath}"

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

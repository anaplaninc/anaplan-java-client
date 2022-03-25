#!/bin/sh
# This example list module list
. ./config/config.sh
CertPath=$CACertPath
PrivateKey="certs/sample_private_key_without_pass.pem:random"
AnaplanUser=$UserCredentials
ActionName=""
ImportName=""
ExportName=""
FileName=""
FilePath="view-data/multipleSignersViewData_245.json"
ModuleId="102000000010"
ViewId="102000999010"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace '${WorkspaceName}' -model '${ModelName}' -mo ${ModuleId} -vi '${ViewId}' -execute -get:json ${FilePath}"

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

if [ "${CertPath}" ]; then
  #Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"
  Credentials="-certificate ${CertPath} -privatekey ${PrivateKey}" # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
  # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

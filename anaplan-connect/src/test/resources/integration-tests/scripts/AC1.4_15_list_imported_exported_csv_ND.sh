#!/bin/sh
# This example runs an export
. ./config/config.sh
AnaplanUser=$UserCredentials
ActionName=""
ImportName=""
ExportName="Grid - coresrv-2775-list.csv"
FileName="export/imported-exported-coresrv-2775-list.csv"
FilePath=""
ChunkSize=1
OutputName=""
ErrorDump=""
nonDefaultModelId
Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${nonDefaultWorkspaceId} -model ${nonDefaultModelId} -export '${ExportName}' -execute -get '${FileName}'"

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

if [ "${CertPath}" ]; then
  Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"
  #Credentials="-certificate ${CertPath} -keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"    # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
  # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

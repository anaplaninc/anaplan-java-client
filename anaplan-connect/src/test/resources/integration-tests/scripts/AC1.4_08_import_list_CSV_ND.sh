#!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh
AnaplanUser=$UserCredentials
ActionName=""
ImportName="coresrv-2775-list from coresrv-2775-list.csv"
ExportName=""
FileName="coresrv-2775-list.csv"
FilePath="data/coresrv-2775-list.csv"
ChunkSize=1
OutputName=""
ErrorDump="dump/coresrv-2775-list from coresrv-2775-list.csv"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${nonDefaultWorkspaceId} -model ${nonDefaultModelId} -chunksize ${ChunkSize} -file '${FileName}' -put ${FilePath} -import '${ImportName}' -execute -output '${ErrorDump}' "

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

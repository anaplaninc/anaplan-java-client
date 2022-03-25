#!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh
AnaplanUser=$UserCredentials
ActionName=""
ImportName="INTEGRA-2682-テスト from INTEGRA-2682.csv"
ExportName=""
FileName="INTEGRA-2682.csv"
FilePath="data/INTEGRA-2682.csv"
ChunkSize=1
OutputName=""
ErrorDump="dump/INTEGRA-2682-テスト from INTEGRA-2682.txt"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId}  -file '${FileName}' -put '${FilePath}' -chunksize ${ChunkSize} -import '${ImportName}' -execute -output '${ErrorDump}' "

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

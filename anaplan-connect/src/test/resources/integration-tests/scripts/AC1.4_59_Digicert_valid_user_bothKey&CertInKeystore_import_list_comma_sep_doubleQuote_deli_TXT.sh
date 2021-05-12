##!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh
CertPath=$CACertPath
ActionName=""
ImportName="ListWithCommaSepDoubQuoDeli from ListWithCommaSepDoubQuoDe~1"
ExportName=""
FileName="ListWithCommaSepDoubQuoDeli.txt"
FilePath="data/ListWithCommaSepDoubQuoDeli.txt"
ChunkSize=1
ErrorDump="dump/ListWithCommaSepDoubQuoDeli from ListWithCommaSepDoubQuoDe~1"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId} -chunksize ${ChunkSize} -file '${FileName}' -put ${FilePath} -import '${ImportName}' -execute -output '${ErrorDump}' "

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

if [ "${CertPath}" ]; then
  #Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"
  Credentials="-certificate ${CertPath} -keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}" # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

FILEPATH="/Users/nayanarajanna/Documents/Nayana_Anaplan/connect/anaplan-connect-1.4/anaplan-connect/import-files/ListWithCommaSepDoubQuoDeli.txt"

Operation="-debug -service 'https://api-stg.anaplan.com' -auth 'https://auth-stg.anaplan.com' -workspace ${WorkspaceId} -model ${ModelId} -chunksize 1 -file 'ListWithCommaSepDoubQuoDeli.txt' -put ${FILEPATH} -import 'ListWithCommaSepDoubQuoDeli from ListWithCommaSepDoubQuoDe~1' -execute -output 'exports/ListWithCommaSepDoubQuoDeli from ListWithCommaSepDoubQuoDe~1' "

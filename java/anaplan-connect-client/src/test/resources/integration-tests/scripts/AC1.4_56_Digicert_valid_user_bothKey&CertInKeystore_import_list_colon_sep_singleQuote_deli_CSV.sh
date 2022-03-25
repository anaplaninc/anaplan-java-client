##!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh
CertPath=$CACertPath
ActionName=""
ImportName="ListWithColonSepSingleQuoDe from ListWithColonSepSingleQuoDe"
ExportName=""
FileName="ListWithColonSepSingleQuoDeli.csv"
FilePath="data/ListWithColonSepSingleQuoDeli.csv"
ChunkSize=1
ErrorDump="dump/ListWithColonSepSingleQuoDe from ListWithColonSepSingleQuoDe"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId} -chunksize ${ChunkSize} -file '${FileName}' -put ${FilePath} -import '${ImportName}' -execute -output '${ErrorDump}' "

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

if [ "${CertPath}" ]; then
  Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

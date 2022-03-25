##!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh
CertPath=$CACertPath
ActionName=""
ImportName="ListWithColonSepDoubleQuoDe from ListWithColonSepDoubleQuoDe"
ExportName=""
FileName="ListWithColonSepDoubleQuoDelicsv.csv"
FilePath="data/ListWithColonSepDoubleQuoDelicsv.csv"
ChunkSize=1
OutputName="dump/ListWithColonSepDoubleQuoDe from ListWithColonSepDoubleQuoDe"

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

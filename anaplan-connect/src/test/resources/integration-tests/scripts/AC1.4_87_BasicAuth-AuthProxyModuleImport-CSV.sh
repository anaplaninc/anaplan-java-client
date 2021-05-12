#!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh
AnaplanUser=$UserCredentials
ActionName=""
ImportName="DepListModuleSmall from DepListModuleSmall-TabSingleCol.csv"
ExportName=""
FileName="DepListModuleSmall-TabSingleCol.csv"
FilePath="data/DepListModuleSmall-TabSingleCol.csv"
ChunkSize=1
OutputName=""
ErrorDump="dump/DepListModuleSmall from DepListModuleSmall-TabSingleCol.csv"
ProxyUrl="http://10.0.88.161:8080"
#ProxyUser=nrajanna:Welcome1

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -via '${ProxyUrl}'  -workspace ${WorkspaceId} -model ${ModelId} -chunksize ${ChunkSize} -file '${FileName}' -put ${FilePath} -import '${ImportName}' -execute -output '${ErrorDump}' "
#Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -via '${ProxyUrl}' -viauser ${ProxyUser} -workspace ${WorkspaceId} -model ${ModelId} -chunksize ${ChunkSize} -file '${FileName}' -put ${FilePath} -import '${ImportName}' -execute -output '${ErrorDump}' "Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -via '${ProxyUrl}' -viauser ${ProxyUser} -workspace ${WorkspaceId} -model ${ModelId} -chunksize ${ChunkSize} -file '${FileName}' -put ${FilePath} -import '${ImportName}' -execute -output '${ErrorDump}' "

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

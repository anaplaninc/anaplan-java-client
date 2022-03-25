#!/bin/sh

. ./config/config.sh
AnaplanUser=$UserCredentials
ImportName="coresrv-2776-list from coresrv-2776-list.txt"
FileName="coresrv-2776-list.txt"
FilePath="data/coresrv-2776-list.txt"
ErrorDump="dump/basic-auth-coresrv-2776-list-txt from coresrv-2776-list nd.txt"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${nonDefaultWorkspaceId} -model ${nonDefaultModelId} -put ${FilePath} -import '${ImportName}' -execute -output '${ErrorDump}'"

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

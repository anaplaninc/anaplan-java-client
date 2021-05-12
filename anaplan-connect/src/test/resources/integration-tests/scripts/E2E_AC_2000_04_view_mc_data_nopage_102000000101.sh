#!/bin/sh
. ./config/config.sh
# This example provides view data in csv format
AnaplanUser=$UserCredentials
FilePath="E2E_AC_2000_Test/E2E_AC_2000_04_view_mc_data_nopage_102000000101.csv"
ModuleId="102000000101"
ViewId="102000000101"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model '${ModelId}' -module ${ModuleId} -execute -get:csv_mc ${FilePath}"

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

if [ "${CertPath}" ]; then
  Credentials="-certificate ${CertPath} -keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

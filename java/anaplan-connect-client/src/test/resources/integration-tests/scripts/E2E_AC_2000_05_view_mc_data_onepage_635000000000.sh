#!/bin/sh
# This example provides view data in csv format
. ./config/config.sh
AnaplanUser=$UserCredentials
FilePath="E2E_AC_2000_Test/E2E_AC_2000_05_view_mc_data_onepage_635000000000.csv"
ModuleName="INTEGRA-3184"
ViewName="INTEGRA-3184-b"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model '${ModelName}' -mo '${ModuleName}' -view '${ViewName}' -execute -get:csv_mc ${FilePath}"

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

if [ "${CertPath}" ]; then
  Credentials="-certificate ${CertPath} -keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}" # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
  # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

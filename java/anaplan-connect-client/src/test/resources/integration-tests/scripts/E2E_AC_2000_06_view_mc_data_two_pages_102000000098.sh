#!/bin/sh
. ./config/config.sh
# This example provides view data in csv format
AnaplanUser=$UserCredentials
FilePath="E2E_AC_2000_Test/E2E_AC_2000_06_view_mc_data_two_pages_102000000098.csv"
ModuleName="complex-module-2pages"
ViewName="default"
#ViewName="102000000098"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace '${WorkspaceName}' -model '${ModelName}' -mo '${ModuleName}' -vi '${ViewName}' -execute -get:csv_mc ${FilePath}"

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

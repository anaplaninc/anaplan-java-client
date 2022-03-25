#!/bin/sh
# This example list module list
. ./config/config.sh
AnaplanUser=$UserCredentials
FilePath="E2E_AC_2003_Test/E2E_AC_2003_04_view_mc_data_2_pages.csv"
ModuleId="complex-module-2pages"
ViewId="default"
Pages="view-data-test-row1:r\ 2,view-data-test-columns:c\,2\,"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace '${WorkspaceName}' -model ${ModelId} -module ${ModuleId} -vi ${ViewId} -pages '${Pages}' -execute -get:csv_mc ${FilePath}"

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

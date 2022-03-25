#!/bin/sh
# This example list module list
. ./config/config.sh
AnaplanUser=$UserCredentials
FilePath="E2E_AC_2003_Test/E2E_AC_2003_13_view_data_3pages_sc.csv"
ModuleName="complex-2-pages-2rows-1cols3Pages"
ViewName="for, Dimention:Tests\:"
Pages="view-data-test-dimension:\:\:ok a\:y\,,view-data-test-columns:co\,lu\: mn 3,view-data-test-row1:r\,1"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace '${WorkspaceName}' -model '${ModelName}' -module '${ModuleName}' -view '${ViewName}' -pages '${Pages}' -execute -get:csv_sc ${FilePath}"

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

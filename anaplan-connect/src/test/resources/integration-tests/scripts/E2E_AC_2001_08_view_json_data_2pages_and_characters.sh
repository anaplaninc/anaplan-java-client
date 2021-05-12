#!/bin/sh
# This example list module list
. ./config/config.sh
AnaplanUser=$UserCredentials
FilePath="E2E_AC_2001_Test/E2E_AC_2001_08_view_json_data_2pages.json"
ModuleName="complex-2-pages-2rows-1cols3Pages"
ViewName="for, Dimention:Tests\:"
Pages="view-data-testlist\\,pages:p1\\,p4"
Pages="view-data\:-testlist\,pages\::p1\\,p4"
Page1="view-data-test-columns"
Dim1="co\,lu\: mn 3"
Page2="view-data-test-dimension"
Dim2="\:\:ok a\:y\,"
Pages="view-data-test-dimension:\:\:ok a\:y\,,view-data-test-columns:co\,lu\: mn 3"

Page3="view-data-test-row1"
DIM3="r,1"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId} -module '${ModuleName}' -view '${ViewName}' -pages '${Pages}' -execute -get:json ${FilePath}"

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

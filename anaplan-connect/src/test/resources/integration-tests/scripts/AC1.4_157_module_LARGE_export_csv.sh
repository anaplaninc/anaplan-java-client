#!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh

#CertPath="/Users/nayanarajanna/Desktop/DigiCert/nayana_rajanna/nayana_rajanna.pem"
#KeyStorePath="/Users/nayanarajanna/Desktop/DigiCert/AC1.4_keystore.jks"
#KeyStorePass="Welcome1"
#KeyStoreAlias="AC1.4Keystore
AnaplanUser=${UserCredentials}
WorkspaceId=${LargeModuleWorkspaceID}
ModelId=${LargeModuleModelID}

Operation="-debug -service '$ServiceUrl' -auth '$AuthUrl' -workspace '${LargeModuleWorkspaceID}' -model '${LargeModuleModelID}' -export 'PerfModule Tabular Single Column.csv' -execute -get 'export/PerfModule Tabular Single Column.csv'"

#____________________________ Do not edit below this line ______________________________

if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

if [ "${CertPath}" ]; then
  Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"
  #Credentials="-certificate ${CertPath} -keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"    # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
  # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
Command="./AnaplanClient.sh  ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

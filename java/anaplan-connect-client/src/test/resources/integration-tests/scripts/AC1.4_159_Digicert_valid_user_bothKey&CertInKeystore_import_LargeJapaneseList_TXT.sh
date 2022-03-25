#!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh
CertPath=$CACertPath

FILEPATH="data/LargeJapaneseList.txt"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId} -chunksize 1 -file 'LargeJapaneseList.txt' -put ${FILEPATH} -import 'LargeJapaneseList from LargeJapaneseList.txt' -execute -output 'dump/LargeJapaneseList from LargeJapaneseList.txt' "

#____________________________ Do not edit below this line ______________________________
if [ "${AnaplanUser}" ]; then
  Credentials="-user ${AnaplanUser}"
fi

if [ "${CertPath}" ]; then
  #Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}"
  Credentials="-keystore ${KeyStorePath} -keystorepass ${KeyStorePass} -keystorealias ${KeyStoreAlias}" # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
  # THIS IS ANOTHER APPROACH OF PROVIDING THE RAW CERTIFICATE VIA -certificate
fi

echo cd "$(dirname "$0")"
cd "$(dirname "$0")"
if [ ! -f AnaplanClient.sh ]; then
  echo "Please ensure this script is in the same directory as AnaplanClient.sh." >&2
  exit 1
elif [ ! -x AnaplanClient.sh ]; then
  echo "Please ensure you have executable permissions on AnaplanClient.sh." >&2
  exit 1
fi

Command="./AnaplanClient.sh ${Credentials} ${Operation} "
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

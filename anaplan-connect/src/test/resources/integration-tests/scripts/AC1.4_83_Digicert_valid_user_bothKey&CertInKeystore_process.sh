#!/bin/sh
# This example uploads a file and runs an import
. ./config/config.sh
CertPath=$CACertPath
ActionName="delete from list"
ImportName=""
ExportName=""
ProcessName="import export delete"
FileName=""
FilePath=""
ChunkSize=""
OutputName=""
ImportDataSource1="coresrv-2776-list.csv"
ImportFileName1="data/coresrv-2776-list.csv"
ImportDataSource2="coresrv-2779-module.csv"
ImportFileName2="data/coresrv-2779-module.csv"
ErrorDump="dump/basic-auth-Process_errors"
ExportName1="coresrv-2779-module - multicol.csv"
ExportName2="coresrv-2775-module - tabularSingleCol.csv"
FileName1="export/certauth-process-coresrv-2779-module - multicol.csv"
FileName2="export/certauth-process-coresrv-2775-module - tabularSingleCol.csv"

Operation="-debug -service ${ServiceUrl} -auth ${AuthUrl} -workspace ${WorkspaceId} -model ${ModelId} -file '${ImportDataSource1}' -put '${ImportFileName1}' -file '${ImportDataSource2}' -put '${ImportFileName2}'  -process '${ProcessName}' -execute -file '${ExportName1}' -get '${FileName1}' -file '${ExportName2}'  -get '${FileName2}' -action '${ActionName}' -execute -output '${ErrorDump}'"

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

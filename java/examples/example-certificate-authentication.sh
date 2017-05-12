#!/bin/sh
# This example lists a user's workspaces

ServiceLocation=https://api.anaplan.com/
Keystore=/path/to/keystore.jks
KeystoreAlias=alias
KeystorePassword=password
Operation="-W"

#____________________________ Do not edit below this line ______________________________

echo cd "`dirname "$0"`"
cd "`dirname "$0"`"
if [ ! -f AnaplanClient.sh ]; then
  echo "Please ensure this script is in the same directory as AnaplanClient.sh." >&2
  exit 1
elif [ ! -x AnaplanClient.sh ]; then
  echo "Please ensure you have executable permissions on AnaplanClient.sh." >&2
  exit 1
fi
Command="./AnaplanClient.sh -s ${ServiceLocation} -k ${Keystore} -ka ${KeystoreAlias} -kp ${KeystorePassword} ${Operation}"
/bin/echo "${Command}"
exec /bin/sh -c "${Command}"

#!/bin/sh
# Check a user's permissions in a directory

if [ $# -lt 1 ]; then
  echo "Usage: check-ob-perms.sh directory [username]"
  exit 1
elif [ ! -d "$1" ]; then
  echo "Error: specified directory does not exist: $2"
  exit 1
fi

if [ $# -lt 2 ]; then
  USER=${whoami}
else
  USER=$2
fi

USERID=$(id -u $USER)
USERGROUPS="$(id -G $USER)"
DIRFILES=$(find "$1")

# If find fails, there is no read permission or the file does not exist
if [ $? -eq 1 ]; then
  echo "Error: user $USER does not have read permissions in some files or directories"
  exit 1
fi

IFS='
'

for FILE in $DIRFILES; do

  OWNER=$(stat -t --format=%u "$FILE")
  PERM=$(stat -t --format=%a "$FILE")
  GROUP="$(stat -t --format=%g "$FILE")"

  if [ $USERID -ne $OWNER ]; then
    
    # Check if user belongs to the file's group
    echo $USERGROUPS | grep -q $GROUP
    [ $? -eq 0 ] && BELONGS=1 || BELONGS=0

    # Significant bit
    if [ $BELONGS -eq 1 ]; then
      SBIT=$(echo $PERM | sed 's/.\(.\)./\1/')
    else
      SBIT=$(echo $PERM | sed 's/..\(.\)/\1/')
    fi

    if [ $SBIT -ne 6 ] && [ $SBIT -ne 7 ]; then
      echo "Error: user $USER does not have read or write permissions in file or directory:"
      echo "$FILE"
      exit 1
    fi

  fi

done

#!/bin/bash

export LD_LIBRARY_PATH=/opt/oracle/instantclient_12_1:$LD_LIBRARY_PATH
export PATH=/opt/oracle/instantclient_12_1:$PATH
export OCI_LIB_DIR=/opt/oracle/instantclient_12_1
export OCI_INC_DIR=/opt/oracle/instantclient_12_1/sdk/include

until node test-db.js
do
  echo "Database unreachable"
  sleep 1m
done

node server.js

#!/bin/bash

./assets/entrypoint.sh > /tmp/logs/oracle.txt 2>&1 &

# Wait until the database is running
sleep 1m

bash /opt/ImageIndex/scripts/initCreateUser.sh
bash /opt/ImageIndex/scripts/loadIndexCreate.sh

wait
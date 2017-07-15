# Define ORACLE_HOME environment variable 
export ORACLE_HOME=/opt/oracle/app/product/11.2.0/dbhome_1
# Workaround for the oracle database to work with the JIT compiler.
umount /dev/shm && mount -t tmpfs -o rw,exec,nodev,relatime,size=128M tmpfs /dev/shm
# Create the mdb_insa user.
/opt/oracle/app/product/11.2.0/dbhome_1/bin/sqlplus SYS/oracle@localhost:1521/orcl AS SYSDBA @/opt/ImageIndex/sql/createUser.sql
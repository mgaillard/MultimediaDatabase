CREATE USER mdb_insa IDENTIFIED BY password;
GRANT CONNECT TO mdb_insa;

GRANT UNLIMITED TABLESPACE TO mdb_insa;
GRANT CREATE ANY TABLE TO mdb_insa;
GRANT CREATE ANY OPERATOR TO mdb_insa;
GRANT CREATE ANY PROCEDURE TO mdb_insa;
GRANT CREATE ANY TYPE TO mdb_insa;
GRANT CREATE ANY INDEXTYPE TO mdb_insa;

CALL dbms_java.grant_permission( 'MDB_INSA', 'SYS:java.io.FilePermission', '/tmp/*', 'read,write,delete' );
CALL dbms_java.grant_permission( 'MDB_INSA', 'SYS:java.net.SocketPermission', 'lire-web-api:8080', 'connect,resolve' );
CALL dbms_java.grant_permission( 'MDB_INSA', 'SYS:java.net.SocketPermission', 'liresolr:8983', 'connect,resolve' );

COMMIT;

quit;
/
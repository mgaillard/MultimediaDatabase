# Define ORACLE_HOME environment variable 
export ORACLE_HOME=/opt/oracle/app/product/11.2.0/dbhome_1
# Drop the table and the index
/opt/oracle/app/product/11.2.0/dbhome_1/bin/sqlplus mdb_insa/password@localhost:1521/orcl @/opt/ImageIndex/sql/drop.sql
# Unload the java classes
/opt/oracle/app/product/11.2.0/dbhome_1/bin/dropjava -user mdb_insa/password@localhost:1521:orcl -verbose -recursivejars /opt/ImageIndex/java/FileLogger.java /opt/ImageIndex/java/ImageIndex.java /opt/ImageIndex/java/LireSolrApi.java /opt/ImageIndex/java/LireSolrFeatureVector.java /opt/ImageIndex/java/LireSolrResult.java /opt/ImageIndex/java/LireSolrResultList.java
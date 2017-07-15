CREATE OR REPLACE TYPE ImageIndex AS OBJECT (
  key INTEGER,

  STATIC FUNCTION ODCIGetInterfaces(
    ifclist OUT SYS.ODCIObjectList)
  RETURN NUMBER,

  STATIC FUNCTION ODCIIndexCreate(
    ia SYS.ODCIIndexInfo, 
    parms VARCHAR2, 
    env SYS.ODCIEnv) 
    RETURN NUMBER AS LANGUAGE JAVA
    NAME 'mdb.ImageIndex.ODCIIndexCreate(oracle.ODCI.ODCIIndexInfo, java.lang.String, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexDrop(
    ia SYS.ODCIIndexInfo, 
    env SYS.ODCIEnv)
    RETURN NUMBER AS LANGUAGE JAVA
    NAME 'mdb.ImageIndex.ODCIIndexDrop(oracle.ODCI.ODCIIndexInfo, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexInsert(
    ia SYS.ODCIIndexInfo,
    rid VARCHAR2,
    newval VARCHAR2,
    env SYS.ODCIEnv)
    RETURN NUMBER AS LANGUAGE JAVA
    NAME 'mdb.ImageIndex.ODCIIndexInsert(oracle.ODCI.ODCIIndexInfo, java.lang.String, java.lang.String, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexDelete(
    ia SYS.ODCIIndexInfo,
    rid VARCHAR2,
    oldval VARCHAR2,
    env SYS.ODCIEnv)
    RETURN NUMBER AS LANGUAGE JAVA
    NAME 'mdb.ImageIndex.ODCIIndexDelete(oracle.ODCI.ODCIIndexInfo, java.lang.String, java.lang.String, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexUpdate(
    ia SYS.ODCIIndexInfo,
    rid VARCHAR2,
    oldval VARCHAR2,
    newval VARCHAR2,
    env SYS.ODCIEnv)
    RETURN NUMBER AS LANGUAGE JAVA
    NAME 'mdb.ImageIndex.ODCIIndexUpdate(oracle.ODCI.ODCIIndexInfo, java.lang.String, java.lang.String, java.lang.String, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  STATIC FUNCTION ODCIIndexStart(
    sctx IN OUT ImageIndex,
    ia SYS.ODCIIndexInfo, 
    pi SYS.ODCIPredInfo, 
    qi SYS.ODCIQueryInfo, 
    strt NUMBER,
    stop NUMBER,
    im1 IN VARCHAR2,
    env SYS.ODCIEnv)
    RETURN NUMBER AS LANGUAGE JAVA
    NAME 'mdb.ImageIndex.ODCIIndexStart(oracle.sql.STRUCT[], oracle.ODCI.ODCIIndexInfo, oracle.ODCI.ODCIPredInfo, oracle.ODCI.ODCIQueryInfo, java.math.BigDecimal, java.math.BigDecimal, java.lang.String, oracle.ODCI.ODCIEnv) return java.math.BigDecimal',

  MEMBER FUNCTION ODCIIndexFetch(
    self IN ImageIndex,
    nrows NUMBER,
    rids OUT SYS.ODCIRidList,
    env SYS.ODCIEnv)
    RETURN NUMBER AS LANGUAGE JAVA
    NAME 'mdb.ImageIndex.ODCIIndexFetch(java.math.BigDecimal, oracle.ODCI.ODCIRidList[], oracle.ODCI.ODCIEnv) return java.math.BigDecimal',
    
  MEMBER FUNCTION ODCIIndexClose(
    self IN ImageIndex,
    env SYS.ODCIEnv)
    RETURN NUMBER AS LANGUAGE JAVA
    NAME 'mdb.ImageIndex.ODCIIndexClose(oracle.ODCI.ODCIEnv) return java.math.BigDecimal',
    
  STATIC FUNCTION ODCIIndexAlter(
    ia SYS.ODCIIndexInfo,
    parms VARCHAR2,
    alter_option NUMBER,
    env SYS.ODCIEnv)
    RETURN NUMBER AS LANGUAGE JAVA
    NAME 'mdb.ImageIndex.ODCIIndexAlter(oracle.ODCI.ODCIIndexInfo, java.lang.String, java.math.BigDecimal, oracle.ODCI.ODCIEnv) return java.math.BigDecimal'
)

/

CREATE OR REPLACE TYPE BODY ImageIndex IS
  STATIC FUNCTION ODCIGetInterfaces(
    ifclist OUT SYS.ODCIObjectList)
  RETURN NUMBER IS
  BEGIN
    ifclist := SYS.ODCIObjectList(SYS.ODCIObject('SYS','ODCIINDEX2'));
    RETURN ODCIConst.Success;
  END ODCIGetInterfaces;
END;

/

CREATE OR REPLACE FUNCTION ImageDistanceFunction(
  ImageA IN VARCHAR2,
  ImageB IN VARCHAR2,
  indexctx IN SYS.ODCIIndexCtx,
  scanctx IN OUT ImageIndex,
  scanflg IN NUMBER) 
  RETURN NUMBER AS LANGUAGE JAVA
  NAME 'mdb.ImageIndex.ImageDistance(java.lang.String, java.lang.String, oracle.ODCI.ODCIIndexCtx, oracle.sql.STRUCT[], java.math.BigDecimal) return java.math.BigDecimal';

/

CREATE OR REPLACE OPERATOR ImageDistance 
BINDING (VARCHAR2, VARCHAR2) RETURN NUMBER
WITH INDEX CONTEXT, SCAN CONTEXT ImageIndex COMPUTE ANCILLARY DATA
USING ImageDistanceFunction

/

CREATE OR REPLACE FUNCTION ImageScoreFunction(
  ImageA IN VARCHAR2,
  ImageB IN VARCHAR2,
  indexctx IN SYS.ODCIIndexCtx,
  scanctx IN OUT ImageIndex,
  scanflg IN NUMBER) 
  RETURN NUMBER AS LANGUAGE JAVA
  NAME 'mdb.ImageIndex.ImageScore(java.lang.String, java.lang.String, oracle.ODCI.ODCIIndexCtx, mdb.ImageIndex[], java.math.BigDecimal) return java.math.BigDecimal';

/

CREATE OR REPLACE OPERATOR ImageScore
BINDING (NUMBER) RETURN NUMBER
ANCILLARY TO ImageDistance(VARCHAR2, VARCHAR2) 
USING ImageScoreFunction;

/

CREATE OR REPLACE INDEXTYPE ImageIndexType
FOR ImageDistance(VARCHAR2, VARCHAR2)
USING ImageIndex
WITH SYSTEM MANAGED STORAGE TABLES;

quit;
/
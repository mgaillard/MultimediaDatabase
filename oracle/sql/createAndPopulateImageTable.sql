CREATE TABLE mdb_insa.IMAGES
(
 FILENAME VARCHAR2(256) NOT NULL,
 CONSTRAINT pk_filename PRIMARY KEY (FILENAME)
);

CREATE INDEX ImagesIndex on mdb_insa.IMAGES(FILENAME) INDEXTYPE IS ImageIndexType

/

BEGIN
 FOR i IN 0..9907 LOOP
 INSERT INTO mdb_insa.IMAGES VALUES(CONCAT(CONCAT('http://www.ci.gxnu.edu.cn/cbir/Corel/', TO_CHAR(i)), '.jpg'));
 END LOOP;
 COMMIT WORK;
END;

/

quit;
/
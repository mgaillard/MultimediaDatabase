Multimedia-Datenbanken Project University of Passau
===================================================
This software is a content-based image retrieval system created for an assignement of Multimedia-Datenbanken course (5771UE) at University of Passau, Germany.

Authors
-------
We are a team of three french Students following the double degree between **INSA Lyon**, France and the **University of Passau**, Germany.

Kilian Ollivier - Guillaume Kheng - Mathieu Gaillard

How does it work ?
--------------
By extending a Oracle Database, we allow the user to retrieve images by similarity with SQL queries. To index the images we use a backend based on [LIRE](http://www.lire-project.net/): Lucene Image Retrieval and [Apache Solr](https://github.com/dermotte/liresolr). Instead of saving all the images in the system, we only use their URL on internet. Thus it is possible to index every image available on internet. Moreover it is also possible to query images according to an images on internet.

Dependences
-----------
The project rely on a Oracle database.

Install a Docker container with Oracle Database 11g Release 2. Instructions are on the [jaspeen/oracle-11g](https://github.com/jaspeen/oracle-11g) GitHub repository.

Download the [Oracle Instant Client version 12.1](http://www.oracle.com/technetwork/topics/linuxx86-64soft-092277.html) "Basic" and "SDK" packages. Extract them in the `frontend` directory.

Build
-----
```bash
# Build the database and frontend Dockers
$ docker build -t mgaillard/mdb_oracle oracle
$ docker build -t mgaillard/mdb_frontend frontend
```

Run
---
```bash
# Run the application
$ docker-compose up 
```

Once the database is set up and running, go to [http://localhost](http://localhost)

License
-------
See the LICENSE file.

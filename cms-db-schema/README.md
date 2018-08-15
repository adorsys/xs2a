#How to work with liquibase:

* If you migrate to liquibase from our project versions that are earlier than 1.4, delete all tables and sequences in your postgres database (if you already have them). Better do it with such tools as pgAdmin or Dbeaver to be sure that you deleted everything (or use command line).

* Create new scheme 'consent' in DB 

* Go to module cms-db-schema in the project.

* Copy “liquibase.example.properties”, paste it in the same module near the old one and name it ” liquibase.properties”, so now you will have 2 “properties” files: “liquibase.properties” and ” liquibase.example.properties”.

* Run in the command line: 
```
 mvn liquibase:update
```

 You should see the response “Build success” in the console after performing the update.



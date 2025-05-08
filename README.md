# Ski-Resort-Database
Collaborative JDBC Oracle database project between Luckie Musngi and Aj Becerra

This is the final project for our Database Design class, where we build a database–driven information management system from ground
up.

This project contains a script that creates tables (with a function to populate it with random valid tuples), and another containing a user interface that can queuer the database.

We were given an application domain and a list of queries that must be possible.
The Schema, planning, and application were all done without further assistance.

To run in lectura (start lectura first):

    Add the Oracle JDBC driver to your CLASSPATH environment variable using:
        export CLASSPATH=/usr/lib/oracle/19.8/client64/lib/ojdbc8.jar:${CLASSPATH}

    Compile DBMSetup.java using:
        javac DBMSSetup.java

    Compile Interface.java using:
        javac Interface.java
    
    Run DBMSSetup.java
        java DBMSSetup

    This will both create the tables and populate them with values

    Run Interface.java
        java Interface

    Follow the instructions of this interface to modify and query the database


    For both of these, you will need to sign in with your oracle username and password

    I highly recommend u take advantage of print tables!

Workload Distribution:
    
    Luckie Musngi:
        Worked on creating tables, writing add, constraint checking, populating tables, writing queries, creating schema
        General debugging
    AJ Becerra:
        Worked on creating tables, writing update/delete, constaint checking, writing queries creating schema
        General debugging
        Creation of E-R Diagram and analysis

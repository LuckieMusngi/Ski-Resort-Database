# Ski-Resort-Database
Collaborative JDBC Oracle database project between Luckie Musngi and Aj Becerra

We built a database–driven information management system from the ground up.

This project contains a script that creates tables (with a function to populate it with random valid tuples), and another containing a user interface that can queuer the database.

We were given an application domain and a list of queries that must be possible.
The Schema, planning, and application were all done without further assistance.

To run in lectura (start lectura first):

To run the project:

    Ensure you have Java (JDK 8+) installed

    Ensure you have access to an Oracle database
        (local installation or remote server)

    Download the Oracle JDBC driver (ojdbc8.jar)

    Add the Oracle JDBC driver to your CLASSPATH environment variable:
        export CLASSPATH=/path/to/ojdbc8.jar:${CLASSPATH}

    Compile DBMSSetup.java and Interface.java:
        javac DBMSSetup.java
        javac Interface.java

    Run DBMSSetup.java:
        java DBMSSetup

        This will create the tables and populate them with sample data

    Note: You can also populate them yourself within Interface.java

    Run Interface.java:
        java Interface

    Follow the instructions in the command-line interface to
    query and modify the database

Workload Distribution:
    
    Luckie Musngi:
        Worked on creating tables, writing add, constraint checking, populating tables, writing queries, creating schema
        General debugging
    AJ Becerra:
        Worked on creating tables, writing update/delete, constaint checking, writing queries creating schema
        General debugging
        Creation of E-R Diagram and analysis

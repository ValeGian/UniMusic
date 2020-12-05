package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jDriver {
    private static Neo4jDriver instance = new Neo4jDriver();
    private final Driver driver;

    private final String uri = "neo4j://localhost:7687";
    private final String user = "";
    private final String password = "";

    private Neo4jDriver() {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public static Neo4jDriver getInstance() { return instance; }

    public Driver getDriver() { return driver; }

    public void closeConnection() { driver.close(); }
}

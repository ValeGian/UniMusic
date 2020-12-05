package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.neo4jconnection;

import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.driver.Values.parameters;

public class Neo4jDriver {
    private static Neo4jDriver instance = new Neo4jDriver();
    private final Driver driver;

    private final String uri = "neo4j://localhost:7687";
    private final String user = "neo4j";
    private final String password = "root";

    private Neo4jDriver() {

        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public static Neo4jDriver getInstance() { return instance; }

    public Driver getDriver() { return driver; }

    public void closeConnection() { driver.close(); }

    public static void main(String[] args) {
        final String actorName = "Tom Hanks";
        Driver driver = Neo4jDriver.getInstance().getDriver();
        try ( Session session = driver.session() )
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MERGE (p:Person {name: $name, from: $from, age: $age})",
                        parameters( "name", "Valerio", "from", "Italy", "age", 22 ) );
                return null;
            });
        }

        try ( Session session = driver.session() )
        {
            Integer age = session.readTransaction((TransactionWork<Integer>) tx -> {
                Result result = tx.run( "MATCH (p:Person) WHERE p.name = $name RETURN p.age",
                        parameters( "name", "Valerio") );
                return result.single().get(0).asInt();
            });
            System.out.println(age);
        }
    }
}

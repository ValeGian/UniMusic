package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection;

import com.mongodb.ConnectionString;
import com.mongodb.client.*;

public class MongoDriver {
    private static final MongoDriver instance = new MongoDriver();
    private final MongoClient client;
    private final MongoDatabase mongoDB;

    private final String connectionString = "mongodb://172.16.3.115:27020,172.16.3.114:27020,172.16.3.161:27020/"
            + "?w=1&readPreference=nearest";
    private final String databaseName = "UniMusic";

    private MongoDriver() {
        client = MongoClients.create(new ConnectionString(connectionString));
        // connect with the database
        mongoDB = client.getDatabase(databaseName);
    }

    public static MongoDriver getInstance() { return instance; }

    public MongoCollection getCollection(Collections collectionName) {
        return mongoDB.getCollection(collectionName.toString());
    }

    public void closeConnection() { client.close(); }
}
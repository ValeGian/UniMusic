package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDriver {
    private static MongoDriver instance = new MongoDriver();
    private final MongoClient client;

    private final String connectionString = "mongodb://localhost:27018,localhost:27019,localhost:27020/" +
                                        "?retryWrites=true&w=majority&wtimeout=10000";
    private final String databaseName = "UniMusic";

    private MongoDriver() {
        client = MongoClients.create(connectionString);
    }

    public static MongoDriver getInstance() { return instance; }

    public MongoCollection getCollection(String collectionName) {
        MongoDatabase database = client.getDatabase(databaseName);
        return database.getCollection(collectionName);
    }

    public void closeConnection() { client.close(); }
}

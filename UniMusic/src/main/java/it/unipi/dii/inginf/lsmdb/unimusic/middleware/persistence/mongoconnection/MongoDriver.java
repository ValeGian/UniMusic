package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection;

import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;

public class MongoDriver {
    private static MongoDriver instance = new MongoDriver();
    private final MongoClient client;
    private final MongoDatabase mongoDB;

    /*private final String connectionString = "mongodb://localhost:27018,localhost:27019,localhost:27020/" +
                                        "?retryWrites=true&w=majority&wtimeout=10000";
     */
    private final String connectionString = "mongodb://localhost:27017";
    private final String databaseName = "UniMusic";

    private MongoDriver() {
        client = MongoClients.create(new ConnectionString(connectionString));
        // connect with the database
        mongoDB = client.getDatabase(databaseName);
    }

    public static MongoDriver getInstance() { return instance; }

    public MongoCollection getCollection(String collectionName) {
        return mongoDB.getCollection(collectionName);
    }

    public void closeConnection() { client.close(); }

    public static void main(String[] args){
        MongoCollection<Document> myColl = MongoDriver.getInstance().getCollection("songs");
        System.out.println("Collection opened. Num of Docs: " + myColl.countDocuments());
    }
}
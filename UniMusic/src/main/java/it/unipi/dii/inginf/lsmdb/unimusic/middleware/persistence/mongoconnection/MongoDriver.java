package it.unipi.dii.inginf.lsmdb.unimusic.middleware.persistence.mongoconnection;

import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;

public class MongoDriver {
    private static MongoDriver instance = new MongoDriver();
    private final MongoClient client;

    /*private final String connectionString = "mongodb://localhost:27018,localhost:27019,localhost:27020/" +
                                        "?retryWrites=true&w=majority&wtimeout=10000";
     */
    private final String connectionString = "mongodb://localhost:27018";
    private final String databaseName = "UniMusic";

    private MongoDriver() {
        client = MongoClients.create(new ConnectionString(connectionString));
    }

    public static MongoDriver getInstance() { return instance; }

    public MongoCollection getCollection(String collectionName) {
        MongoDatabase database = client.getDatabase(databaseName);
        return database.getCollection(collectionName);
    }

    public void closeConnection() { client.close(); }

    public static void main(String[] args){
        MongoCollection<Document> myColl = MongoDriver.getInstance().getCollection("songs");
        Document doc = new Document("_id", "SonoDaCancellare")
                .append("title", "Titolo 2");
        myColl.insertOne(doc);
    }
}
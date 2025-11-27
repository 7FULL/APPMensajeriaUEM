package com.appmsg.appmensajeriauem;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


public class MongoDbClient {

    private MongoDatabase database;

    public MongoDbClient() {
        MongoClient client = new MongoClient("localhost", 27017);
        this.database = client.getDatabase("local");
        database.getCollection("users").find().first();
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }
}

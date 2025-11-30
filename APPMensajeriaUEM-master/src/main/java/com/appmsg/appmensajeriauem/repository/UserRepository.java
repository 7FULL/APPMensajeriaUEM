package com.appmsg.appmensajeriauem.repository;

import com.appmsg.appmensajeriauem.MongoDbClient;
import com.appmsg.appmensajeriauem.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class UserRepository {
    private final MongoCollection<Document> collection;

    public UserRepository(MongoDbClient mongoClient) {
        this.collection = mongoClient.getCollection("User");
    }

    public void createUser(User user) {
        Document doc = new Document("password", user.getPassword())
                .append("picture", user.getPicture())
                .append("username", user.getUsername())
                .append("token", user.getToken())
                .append("email", user.getEmail())
                .append("status", user.getStatus())
                .append("wallpaper", user.getWallpaper());

        collection.insertOne(doc);
        System.out.println("User created");
    }

    public User getUserByEmail(String email) {
        Document doc = collection.find(Filters.eq("email", email)).first();
        if (doc == null) return null;

        return documentToUser(doc);
    }

    public void updateUser(ObjectId id, String field, Object value) {
        collection.updateOne(
                Filters.eq("_id", id),
                new Document("$set", new Document(field, value))
        );
        System.out.println("User updated");
    }

    public void deleteUser(String email) {
        collection.deleteOne(Filters.eq("email", email));
        System.out.println("User with email " + email +" deleted");
    }

    private User documentToUser(Document doc) {
        return new User(
                doc.getObjectId("_id"),
                doc.getString("password"),
                doc.getString("picture"),
                doc.getString("username"),
                doc.getString("token"),
                doc.getString("email"),
                doc.getString("status"),
                doc.getString("wallpaper")
        );
    }
}

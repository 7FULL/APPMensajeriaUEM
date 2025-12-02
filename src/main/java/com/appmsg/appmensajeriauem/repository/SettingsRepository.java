package com.appmsg.appmensajeriauem.repository;

import com.appmsg.appmensajeriauem.MongoDbClient;
import com.appmsg.appmensajeriauem.model.UserSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

public class SettingsRepository {

    private final MongoCollection<Document> collection;

    public SettingsRepository(MongoDbClient mongoClient) {
        this.collection = mongoClient.getCollection("UserSettings");
    }

    public UserSettings getByUserId(ObjectId userId) {
        Document doc = collection.find(Filters.eq("userId", userId)).first();
        return doc != null ? documentToSettings(doc) : null;
    }

    public UserSettings save(UserSettings settings) {
        Document doc = new Document()
                .append("userId", settings.getUserId())
                .append("darkMode", settings.isDarkMode())
                .append("wallpaperPath", settings.getWallpaperPath())
                .append("displayName", settings.getDisplayName())
                .append("status", settings.getStatus());

        if (settings.getId() == null) {
            collection.insertOne(doc);
            settings.setId(doc.getObjectId("_id"));
        } else {
            collection.replaceOne(Filters.eq("_id", settings.getId()), doc);
        }

        return settings;
    }

    private UserSettings documentToSettings(Document doc) {
        return new UserSettings(
                doc.getObjectId("_id"),
                doc.getObjectId("userId"),
                doc.getBoolean("darkMode", false),
                doc.getString("wallpaperPath"),
                doc.getString("displayName"),
                doc.getString("status")
        );
    }
}

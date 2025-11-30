package com.appmsg.appmensajeriauem.repository;

import com.appmsg.appmensajeriauem.MongoDbClient;
import com.appmsg.appmensajeriauem.model.Chat;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatRepository {
    private final MongoCollection<Document> collection;

    public ChatRepository(MongoDbClient mongoClient) {
        this.collection = mongoClient.getCollection("Chat");
    }

    public Chat startPrivateConversation(ObjectId user1Id, ObjectId user2Id) {
        if (user1Id == null || user2Id == null) {
            throw new IllegalArgumentException("User IDs cannot be null");
        }

        // Verificar si ya existe un chat entre estos dos usuarios
        Chat existingChat = findPrivateChat(user1Id, user2Id);
        if (existingChat != null) {
            return existingChat;
        }

        // Crear nuevo chat privado (sin nombre, solo 2 usuarios)
        List<ObjectId> userList = Arrays.asList(user1Id, user2Id);
        Chat newChat = new Chat(null, null, userList, null);

        Document doc = new Document()
                .append("chatName", null)
                .append("userList", userList)
                .append("chatImage", null);

        collection.insertOne(doc);
        newChat.setId(doc.getObjectId("_id"));

        System.out.println("Private conversation started");
        return newChat;
    }


    public Chat createGroup(String groupName, List<ObjectId> userIds, String groupImage) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }
        if (userIds == null || userIds.size() < 2) {
            throw new IllegalArgumentException("Group must have at least 2 users");
        }

        Chat newGroup = new Chat(null, groupName, userIds, groupImage);

        Document doc = new Document()
                .append("chatName", groupName)
                .append("userList", userIds)
                .append("chatImage", groupImage);

        collection.insertOne(doc);
        newGroup.setId(doc.getObjectId("_id"));

        System.out.println("Group created: " + groupName);
        return newGroup;
    }

    public Chat enterConversation(ObjectId chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID cannot be null");
        }

        Document doc = collection.find(Filters.eq("_id", chatId)).first();
        if (doc == null) {
            return null;
        }

        return documentToChat(doc);
    }

    private Chat findPrivateChat(ObjectId user1Id, ObjectId user2Id) {
        Document doc = collection.find(
                Filters.and(
                        Filters.eq("chatName", null),
                        Filters.all("userList", Arrays.asList(user1Id, user2Id)),
                        Filters.size("userList", 2)
                )
        ).first();

        return doc != null ? documentToChat(doc) : null;
    }

    public Chat getChatById(ObjectId chatId) {
        if (chatId == null) {
            return null;
        }

        Document doc = collection.find(Filters.eq("_id", chatId)).first();
        if (doc == null) {
            return null;
        }

        return documentToChat(doc);
    }

    private Chat documentToChat(Document doc) {
        if (doc == null) {
            return null;
        }

        return new Chat(
                doc.getObjectId("_id"),
                doc.getString("chatName"),
                doc.getList("userList", ObjectId.class),
                doc.getString("chatImage")
        );
    }
}

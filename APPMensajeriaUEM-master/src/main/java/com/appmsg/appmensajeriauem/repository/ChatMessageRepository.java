package com.appmsg.appmensajeriauem.repository;

import com.appmsg.appmensajeriauem.MongoDbClient;
import com.appmsg.appmensajeriauem.model.ChatMessage;
import com.appmsg.appmensajeriauem.model.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.*;

public class ChatMessageRepository {
    private final MongoCollection<Document> collection;

    public ChatMessageRepository(MongoDbClient mongoClient) {
        this.collection = mongoClient.getCollection("ChatMessage");
    }

    // Enviar un mensaje
    public void sendMessage(ChatMessage chatMessage) {
        Document doc = new Document()
                .append("chatId", chatMessage.getChatId())
                .append("senderId", chatMessage.getSenderId())
                .append("messageId",chatMessage.getMessageId())
                .append("message",chatMessage.getMessage())
                .append("multimedia",Arrays.asList(chatMessage.getMultimedia()))
                .append("status", chatMessage.getStatus())
                .append("timestamp",chatMessage.getTimestamp())
                .append("updated",chatMessage.getUpdated());

        collection.insertOne(doc);
        System.out.println("Chat message sent");
    }

   /* public List<ChatMessage> getMessages(ObjectId chatId, int limit) {
        List<ChatMessage> chatmessage = new ArrayList<>();
        collection.find(Filters.eq("chatId", chatId))
                .sort(new Document("timestamp", -1))
                .limit(limit)
                .forEach(doc -> chatmessage.add(documentToMessage(doc)));
        return chatmessage;
    } */


    private ChatMessage documentToMessage(Document doc) {
        // Handle multimedia array properly
        List<String> multimediaList = doc.getList("multimedia", String.class);
        String[] multimedia = multimediaList != null
                ? multimediaList.toArray(new String[0])
                : new String[0];

        // Handle timestamp conversion
        java.util.Date date = doc.getDate("timestamp");
        Timestamp timestamp = date != null
                ? new Timestamp(date.getTime())
                : null;

        return new ChatMessage(
                doc.getObjectId("chatId"),
                doc.getObjectId("senderId"),
                doc.getObjectId("messageId"),
                doc.getString("message"),
                multimedia,
                doc.getString("status"),
                timestamp,
                doc.getBoolean("updated")
        );
    }

}

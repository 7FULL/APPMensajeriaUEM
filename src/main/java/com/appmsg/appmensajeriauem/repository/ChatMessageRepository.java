package com.appmsg.appmensajeriauem.repository;

import com.appmsg.appmensajeriauem.MongoDbClient;
import com.appmsg.appmensajeriauem.model.ChatMessage;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.Timestamp;
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

    // Obtener mensajes de un chat
    public List<ChatMessage> getMessages(ObjectId chatId, int limit) {
        List<ChatMessage> chatmessage = new ArrayList<>();
        collection.find(Filters.eq("chatId", chatId))
                .sort(new Document("timestamp", -1))
                .limit(limit)
                .forEach((Block<? super Document>) doc -> chatmessage.add(documentToMessage(doc)));
        return chatmessage;
    }

    // Obtener todos los mensajes de un chat (sin límite)
    public List<ChatMessage> getAllMessages(ObjectId chatId) {
        List<ChatMessage> chatmessage = new ArrayList<>();
        collection.find(Filters.eq("chatId", chatId))
                .sort(new Document("timestamp", 1))
                .forEach((Block<? super Document>) doc -> chatmessage.add(documentToMessage(doc)));
        return chatmessage;
    }


    private ChatMessage documentToMessage(Document doc) {
        // Handle multimedia array properly
        List<String> multimediaList = doc.getList("multimedia", String.class);
        List<String> multimedia = multimediaList != null
                ? multimediaList
                : new ArrayList<>();

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

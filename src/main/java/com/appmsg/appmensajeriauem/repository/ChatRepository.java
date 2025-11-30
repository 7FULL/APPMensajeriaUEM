package com.appmsg.appmensajeriauem.repository;

import com.appmsg.appmensajeriauem.MongoDbClient;
import com.appmsg.appmensajeriauem.model.Chat;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.List;

public class ChatRepository {
    private final MongoCollection<Document> collection;

    //Get chat collection from the database
    public ChatRepository(MongoDbClient mongoClient) {
        this.collection = mongoClient.getCollection("Chat");
    }


    //Create a new chat (group or private)
    public Chat createChat(String chatName,List<ObjectId> userList, String chatImage) {

        //Check that this chat does not already exist
        //The commented lines on this method would imply that a second chat with the same name could be created if the user list is different
        Document dok = collection.find(
                Filters.and(
                        Filters.eq("chatName", chatName)
//                        Filters.all("userList", userList),
//                        Filters.size("userList", userList.size())
                )
        ).first();

        if (dok != null) {
            throw new IllegalArgumentException("Class: ChatRepository, Method: createChat, Error: This chat already exists");
        }

        //Check that the chat name is not null
        if(chatName == null || chatName.trim().isEmpty()){
            throw new IllegalArgumentException("Class: ChatRepository, Method: createChat, Error: The chat name cannot be null or empty, it must have a name");
        }

        //Check that the user list is not null
        if(userList == null){
            throw new IllegalArgumentException("Class: ChatRepository, Method: createChat, Error: The user list cannot be null");
        }

        //Check that there are at least 2 users in the list
        if(userList.size() < 2){
            throw new IllegalArgumentException("Class; ChatRepository, Method: createChat, Error: The user list must have at least 2 users");
        }

        //Check that there is no duplicate user in the list
        if(userList.size() != userList.stream().distinct().count()){
            throw new IllegalArgumentException("Class: ChatRepository, Method: createChat, Error: The user list cannot contain duplicate users");
        }

        //Check if it is required to assign a default chat image
        if(chatImage == null){
            chatImage = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png";
        }

        //check if the chat is a group
        boolean isGroup = false;

        if(userList.size() > 2){
            isGroup = true;
        }

        //Create the new chat
        Chat newChat = new Chat(null, chatName, userList, chatImage, isGroup);

        Document doc = new Document()
                .append("chatName", chatName)
                .append("userList", userList)
                .append("chatImage", chatImage)
                .append("isGroup", isGroup);

        collection.insertOne(doc);

        //Obtain the id of the new chat assigned by MongoDB
        newChat.setId(doc.getObjectId("_id"));

        //Log the conversation type created
        if(isGroup){
            System.out.println("Log: Group conversation named " + chatName + " has been saccessfuly created");
        } else {
            System.out.println("Log: Private conversation named " + chatName + " has been saccessfuly created");
        }


        return newChat;
    }

    //Delete a chat by its id
    public boolean deleteChat(ObjectId chatId) {

        //Delete the chat document by its id
        collection.deleteOne(Filters.eq("_id", chatId));

        //Search for the chat document by the chatId given. If found, it returns true, otherwise false
        if(collection.countDocuments(Filters.eq("_id", chatId)) == 0){
            return true;
        } else {
            throw new IllegalArgumentException("Class: ChatRepository, Method: deleteChat, Error: The chat was not deleted successfuly");
        }
    }


    //Access a chat by its id
    public Chat enterChat(ObjectId chatId) {

        //Check that the chat id is not null
        if (chatId == null) {
            throw new IllegalArgumentException("Class: ChatRepository, Method: enterChat, Error: The chat id cannot be null");
        }

        //Search for the chat document by the chatId given. If found, it brings the whole chat document
        Document doc = collection.find(Filters.eq("_id", chatId)).first();
        if (doc == null) {return null;}

        return documentToChat(doc);
    }


    public Chat getChatById(ObjectId chatId) {
        //Check that the chat id is not null
        if (chatId == null) {
            throw new IllegalArgumentException("Class: ChatRepository, Method: enterChat, Error: The chat id cannot be null");
        }

        //Search for the chat document by the chatId given. If found, it brings the whole chat document
        Document doc = collection.find(Filters.eq("_id", chatId)).first();
        if (doc == null) {return null;}

        return documentToChat(doc);
    }

    private Chat documentToChat(Document doc) {
        if (doc == null) {return null;}

        return new Chat(
                doc.getObjectId("_id"),
                doc.getString("chatName"),
                doc.getList("userList", ObjectId.class),
                doc.getString("chatImage"),
                doc.getBoolean("isGroup")
        );
    }
}

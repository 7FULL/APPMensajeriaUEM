package com.appmsg.appmensajeriauem.model;

import org.bson.types.ObjectId;
import java.util.*;

public class Chat {

    private ObjectId _id;
    private String _chatName;
    private List<ObjectId> _userList;
    private String _chatImage;

    public Chat() {}

    public Chat(ObjectId id, String chatName, List<ObjectId> userList, String chatImage) {
        this._id = id;
        this._chatName = chatName;
        this._userList = userList;
        this._chatImage = chatImage;
    }

    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId id) {
        this._id = id;
    }

    public String getChatName() {
        return _chatName;
    }

    public void setChatName(String chatName) {
        this._chatName = chatName;
    }

    public List<ObjectId> getUserList() {
        return _userList;
    }

    public void setUserList(List<ObjectId> userList) {
        this._userList = userList;
    }

    public void addChatParticipant(ObjectId id){
        this._userList.add(id);
    }

    public void removeChatParticipant(ObjectId id){
        this._userList.remove(id);
    }

    public String getChatImage() {
        return _chatImage;
    }

    public void setChatImage(String chatImage) {
        this._chatImage = chatImage;
    }

    @Override
    public String toString() {
        return "Chat{" +
               "_id=" + _id + '\'' +
               ", _chatId=" + _chatName + '\'' +
               ", _userList=" + _userList + '\'' +
               ", _chatImage=" + _chatImage + '\'' +
               '}';
    }
}

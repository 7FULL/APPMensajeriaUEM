package com.appmsg.appmensajeriauem.model;

import org.bson.types.ObjectId;
import java.sql.Timestamp;

public class ChatMessage {
    private ObjectId _chatId;
    private ObjectId _senderId;
    private ObjectId _messageId;
    private String _message;
    private String[] _multimedia; //Esta definición es correcta?
    private String _status;
    private Timestamp _timestamp;
    private Boolean _updated;

    public ChatMessage(){}

    public ChatMessage(ObjectId chatId, ObjectId senderId, ObjectId messageId, String message,
                       String[] multimedia, String status, Timestamp timestamp, Boolean updated) {
        this._chatId = chatId;
        this._senderId = senderId;
        this._messageId = messageId;
        this._message = message;
        this._multimedia = multimedia;
        this._status = status;
        this._timestamp = timestamp;
        this._updated = updated;
    }

    public ObjectId getChatId() {
        return _chatId;
    }

    //Al hacer esto, no se duplica el id de chat ya creado en la clase chat?
    public void setChatId(ObjectId chatId) {
        this._chatId = chatId;
    }

    public ObjectId getSenderId() {
        return _senderId;
    }

    public void setSenderId(ObjectId senderId) {
        this._senderId = senderId;
    }

    public ObjectId getMessageId() {
        return _messageId;
    }
    public void setMessageId(ObjectId messageId) {
        this._messageId = messageId;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        this._message = message;
    }
    public String[] getMultimedia() {
        return _multimedia;
    }

    public void setMultimedia(String[] multimedia) {
        this._multimedia = multimedia;
    }

    public String getStatus() {
        return _status;
    }
    public void setStatus(String status) {
        this._status = status;
    }
    public Timestamp getTimestamp() {
        return _timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this._timestamp = timestamp;
    }
    public Boolean getUpdated() {
        return _updated;
    }
    public void setUpdated(Boolean updated) {
        this._updated = updated;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "_chatId=" + _chatId + '\'' +
                ", _senderId=" + _senderId + '\'' +
                ", _messageId=" + _messageId + '\'' +
                ", _message='" + _message + '\'' +
                ", _multimedia=" + _multimedia + '\'' +
                ", _status='" + _status + '\'' +
                ", _timestamp=" + _timestamp + '\'' +
                ", _updated=" + _updated + '\'' +
                '}';
    }

}

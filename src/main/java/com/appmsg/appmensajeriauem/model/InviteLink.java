package com.appmsg.appmensajeriauem.model;

import org.bson.types.ObjectId;
import java.sql.Timestamp;

public class InviteLink {
    private ObjectId _id;
    private ObjectId _chatId;
    private String _inviteCode;
    private ObjectId _createdBy;
    private Timestamp _createdAt;
    private Timestamp _expiresAt;
    private Integer _maxUses;
    private Integer _currentUses;
    private Boolean _active;

    public InviteLink() {}

    public InviteLink(ObjectId id, ObjectId chatId, String inviteCode, ObjectId createdBy,
                      Timestamp createdAt, Timestamp expiresAt, Integer maxUses,
                      Integer currentUses, Boolean active) {
        this._id = id;
        this._chatId = chatId;
        this._inviteCode = inviteCode;
        this._createdBy = createdBy;
        this._createdAt = createdAt;
        this._expiresAt = expiresAt;
        this._maxUses = maxUses;
        this._currentUses = currentUses;
        this._active = active;
    }

    public ObjectId getId() { return _id; }
    public ObjectId getChatId() { return _chatId; }
    public String getInviteCode() { return _inviteCode; }
    public ObjectId getCreatedBy() { return _createdBy; }
    public Timestamp getCreatedAt() { return _createdAt; }
    public Timestamp getExpiresAt() { return _expiresAt; }
    public Integer getMaxUses() { return _maxUses; }
    public Integer getCurrentUses() { return _currentUses; }
    public Boolean getActive() { return _active; }

    public void setId(ObjectId id) { this._id = id; }
    public void setChatId(ObjectId chatId) { this._chatId = chatId; }
    public void setInviteCode(String inviteCode) { this._inviteCode = inviteCode; }
    public void setCreatedBy(ObjectId createdBy) { this._createdBy = createdBy; }
    public void setCreatedAt(Timestamp createdAt) { this._createdAt = createdAt; }
    public void setExpiresAt(Timestamp expiresAt) { this._expiresAt = expiresAt; }
    public void setMaxUses(Integer maxUses) { this._maxUses = maxUses; }
    public void setCurrentUses(Integer currentUses) { this._currentUses = currentUses; }
    public void setActive(Boolean active) { this._active = active; }

    public boolean isValid() {
        if (!_active) return false;

        if (_expiresAt != null && System.currentTimeMillis() > _expiresAt.getTime()) {
            return false;
        }

        if (_maxUses != null && _currentUses >= _maxUses) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "InviteLink{" +
                "_id=" + _id +
                ", _chatId=" + _chatId +
                ", _inviteCode='" + _inviteCode + '\'' +
                ", _createdBy=" + _createdBy +
                ", _createdAt=" + _createdAt +
                ", _expiresAt=" + _expiresAt +
                ", _maxUses=" + _maxUses +
                ", _currentUses=" + _currentUses +
                ", _active=" + _active +
                '}';
    }
}

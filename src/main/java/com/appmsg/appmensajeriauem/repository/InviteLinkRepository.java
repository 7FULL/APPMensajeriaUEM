package com.appmsg.appmensajeriauem.repository;

import com.appmsg.appmensajeriauem.MongoDbClient;
import com.appmsg.appmensajeriauem.model.InviteLink;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InviteLinkRepository {
    private final MongoCollection<Document> collection;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 12;
    private static final SecureRandom random = new SecureRandom();

    public InviteLinkRepository(MongoDbClient mongoClient) {
        this.collection = mongoClient.getCollection("InviteLink");
    }

    public InviteLink createInviteLink(ObjectId chatId, ObjectId createdBy,
                                       Integer expirationHours, Integer maxUses) {

        String inviteCode = generateUniqueCode();
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        Timestamp expiresAt = null;

        if (expirationHours != null && expirationHours > 0) {
            long expirationMillis = createdAt.getTime() + (expirationHours * 60L * 60L * 1000L);
            expiresAt = new Timestamp(expirationMillis);
        }

        InviteLink inviteLink = new InviteLink(
                new ObjectId(),
                chatId,
                inviteCode,
                createdBy,
                createdAt,
                expiresAt,
                maxUses,
                0,
                true
        );

        Document doc = new Document()
                .append("_id", inviteLink.getId())
                .append("chatId", chatId)
                .append("inviteCode", inviteCode)
                .append("createdBy", createdBy)
                .append("createdAt", createdAt)
                .append("expiresAt", expiresAt)
                .append("maxUses", maxUses)
                .append("currentUses", 0)
                .append("active", true);

        collection.insertOne(doc);
        System.out.println("Invite link created: " + inviteCode);

        return inviteLink;
    }

    public InviteLink getInviteLinkByCode(String inviteCode) {
        Document doc = collection.find(Filters.eq("inviteCode", inviteCode)).first();
        if (doc == null) return null;

        return documentToInviteLink(doc);
    }

    public List<InviteLink> getInviteLinksByChat(ObjectId chatId) {
        List<InviteLink> links = new ArrayList<>();
        collection.find(Filters.eq("chatId", chatId))
                .forEach((Block<? super Document>) doc -> links.add(documentToInviteLink(doc)));
        return links;
    }

    public void incrementUses(String inviteCode) {
        collection.updateOne(
                Filters.eq("inviteCode", inviteCode),
                new Document("$inc", new Document("currentUses", 1))
        );
        System.out.println("Incremented uses for invite: " + inviteCode);
    }

    public void deactivateInviteLink(String inviteCode) {
        collection.updateOne(
                Filters.eq("inviteCode", inviteCode),
                new Document("$set", new Document("active", false))
        );
        System.out.println("Deactivated invite link: " + inviteCode);
    }

    public void deleteInviteLink(String inviteCode) {
        collection.deleteOne(Filters.eq("inviteCode", inviteCode));
        System.out.println("Deleted invite link: " + inviteCode);
    }

    private String generateUniqueCode() {
        String code;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            code = generateRandomCode();
            attempts++;

            if (getInviteLinkByCode(code) == null) {
                return code;
            }

            if (attempts >= maxAttempts) {
                throw new RuntimeException("No se pudo generar un código único después de " + maxAttempts + " intentos");
            }

        } while (true);
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private InviteLink documentToInviteLink(Document doc) {
        Timestamp expiresAt = null;
        if (doc.getDate("expiresAt") != null) {
            expiresAt = new Timestamp(doc.getDate("expiresAt").getTime());
        }

        Timestamp createdAt = null;
        if (doc.getDate("createdAt") != null) {
            createdAt = new Timestamp(doc.getDate("createdAt").getTime());
        }

        return new InviteLink(
                doc.getObjectId("_id"),
                doc.getObjectId("chatId"),
                doc.getString("inviteCode"),
                doc.getObjectId("createdBy"),
                createdAt,
                expiresAt,
                doc.getInteger("maxUses"),
                doc.getInteger("currentUses"),
                doc.getBoolean("active")
        );
    }
}

package com.appmsg.appmensajeriauem.websocket;

import com.appmsg.appmensajeriauem.MongoDbClient;
import com.appmsg.appmensajeriauem.model.ChatMessage;
import com.appmsg.appmensajeriauem.repository.ChatMessageRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.bson.types.ObjectId;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.Timestamp;

@ServerEndpoint("/chat/{chatId}/{userId}")
public class ChatWebSocket {

    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ChatMessageRepository messageRepository = new ChatMessageRepository(new MongoDbClient());
    private final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("chatId") String chatId,
                       @PathParam("userId") String userId) {

        System.out.println("Nueva conexión: Usuario " + userId + " en chat " + chatId);

        sessionManager.addSession(chatId, userId, session);

        JsonObject notification = new JsonObject();
        notification.addProperty("type", "user_connected");
        notification.addProperty("userId", userId);
        notification.addProperty("chatId", chatId);
        notification.addProperty("timestamp", System.currentTimeMillis());

        sessionManager.broadcastToChat(chatId, gson.toJson(notification));
    }

    @OnMessage
    public void onMessage(String message, Session session,
                          @PathParam("chatId") String chatId,
                          @PathParam("userId") String userId) {

        System.out.println("Mensaje recibido de usuario " + userId + " en chat " + chatId);

        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String messageType = json.has("type") ? json.get("type").getAsString() : "message";

            switch (messageType) {
                case "message":
                    handleChatMessage(json, chatId, userId);
                    break;

                case "typing":
                    handleTypingIndicator(json, chatId, userId);
                    break;

                case "status":
                    handleStatusUpdate(json, chatId, userId);
                    break;

                default:
                    sendError(session, "Tipo de mensaje desconocido: " + messageType);
            }

        } catch (JsonSyntaxException e) {
            sendError(session, "Formato de mensaje inválido");
        } catch (Exception e) {
            System.err.println("Error procesando mensaje: " + e.getMessage());
            e.printStackTrace();
            sendError(session, "Error procesando mensaje");
        }
    }

    private void handleChatMessage(JsonObject json, String chatId, String userId) {
        if (!json.has("message")) {
            System.err.println("Mensaje sin contenido");
            return;
        }

        String messageText = json.get("message").getAsString();

        ObjectId chatObjectId = new ObjectId(chatId);
        ObjectId senderObjectId = new ObjectId(userId);
        ObjectId messageId = new ObjectId();

        String[] multimedia = new String[0];
        if (json.has("multimedia") && json.get("multimedia").isJsonArray()) {
            multimedia = gson.fromJson(json.get("multimedia"), String[].class);
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String status = "sent";

        ChatMessage chatMessage = new ChatMessage(
                chatObjectId, senderObjectId, messageId,
                messageText, multimedia, status, timestamp, false
        );

        messageRepository.sendMessage(chatMessage);

        JsonObject response = new JsonObject();
        response.addProperty("type", "message");
        response.addProperty("messageId", messageId.toString());
        response.addProperty("chatId", chatId);
        response.addProperty("senderId", userId);
        response.addProperty("message", messageText);
        response.add("multimedia", json.has("multimedia") ? json.get("multimedia") : gson.toJsonTree(new String[0]));
        response.addProperty("status", status);
        response.addProperty("timestamp", timestamp.getTime());

        sessionManager.broadcastToChat(chatId, gson.toJson(response));

        System.out.println("Mensaje enviado y guardado: " + messageId);
    }

    private void handleTypingIndicator(JsonObject json, String chatId, String userId) {
        boolean isTyping = json.has("isTyping") && json.get("isTyping").getAsBoolean();

        JsonObject response = new JsonObject();
        response.addProperty("type", "typing");
        response.addProperty("chatId", chatId);
        response.addProperty("userId", userId);
        response.addProperty("isTyping", isTyping);

        sessionManager.broadcastToChat(chatId, gson.toJson(response));
    }

    private void handleStatusUpdate(JsonObject json, String chatId, String userId) {
        if (!json.has("messageId") || !json.has("status")) {
            System.err.println("Status update sin messageId o status");
            return;
        }

        String messageId = json.get("messageId").getAsString();
        String status = json.get("status").getAsString();

        JsonObject response = new JsonObject();
        response.addProperty("type", "status");
        response.addProperty("chatId", chatId);
        response.addProperty("messageId", messageId);
        response.addProperty("status", status);
        response.addProperty("userId", userId);

        sessionManager.broadcastToChat(chatId, gson.toJson(response));
    }

    @OnClose
    public void onClose(Session session,
                        @PathParam("chatId") String chatId,
                        @PathParam("userId") String userId) {

        System.out.println("Desconexión: Usuario " + userId + " de chat " + chatId);

        sessionManager.removeSession(session);

        JsonObject notification = new JsonObject();
        notification.addProperty("type", "user_disconnected");
        notification.addProperty("userId", userId);
        notification.addProperty("chatId", chatId);
        notification.addProperty("timestamp", System.currentTimeMillis());

        sessionManager.broadcastToChat(chatId, gson.toJson(notification));
    }

    @OnError
    public void onError(Session session, Throwable throwable,
                        @PathParam("chatId") String chatId,
                        @PathParam("userId") String userId) {

        System.err.println("Error en WebSocket - Usuario: " + userId + ", Chat: " + chatId);
        throwable.printStackTrace();

        sessionManager.removeSession(session);
    }

    private void sendError(Session session, String errorMessage) {
        if (session.isOpen()) {
            try {
                JsonObject error = new JsonObject();
                error.addProperty("type", "error");
                error.addProperty("message", errorMessage);

                session.getBasicRemote().sendText(gson.toJson(error));
            } catch (IOException e) {
                System.err.println("Error enviando mensaje de error: " + e.getMessage());
            }
        }
    }
}

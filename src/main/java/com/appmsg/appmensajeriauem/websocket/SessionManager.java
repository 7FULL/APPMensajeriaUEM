package com.appmsg.appmensajeriauem.websocket;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor centralizado de sesiones WebSocket
 * Mantiene un registro de qué usuarios están conectados a qué chats
 */
public class SessionManager {

    private static SessionManager instance;

    private final Map<String, Map<String, Session>> chatSessions;
    private final Map<String, UserSession> sessionLookup;

    private SessionManager() {
        this.chatSessions = new ConcurrentHashMap<>();
        this.sessionLookup = new ConcurrentHashMap<>();
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void addSession(String chatId, String userId, Session session) {
        chatSessions.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>())
                    .put(userId, session);

        sessionLookup.put(session.getId(), new UserSession(chatId, userId, session));

        System.out.println("Usuario " + userId + " conectado al chat " + chatId);
    }

    public void removeSession(Session session) {
        UserSession userSession = sessionLookup.remove(session.getId());

        if (userSession != null) {
            Map<String, Session> chatUsers = chatSessions.get(userSession.chatId);
            if (chatUsers != null) {
                chatUsers.remove(userSession.userId);

                if (chatUsers.isEmpty()) {
                    chatSessions.remove(userSession.chatId);
                }
            }

            System.out.println("Usuario " + userSession.userId + " desconectado del chat " + userSession.chatId);
        }
    }

    public void broadcastToChat(String chatId, String message) {
        Map<String, Session> sessions = chatSessions.get(chatId);

        if (sessions == null || sessions.isEmpty()) {
            System.out.println("No hay usuarios conectados al chat " + chatId);
            return;
        }

        List<String> disconnectedUsers = new ArrayList<>();

        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            Session session = entry.getValue();

            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("Error enviando mensaje a usuario " + entry.getKey() + ": " + e.getMessage());
                    disconnectedUsers.add(entry.getKey());
                }
            } else {
                disconnectedUsers.add(entry.getKey());
            }
        }

        for (String userId : disconnectedUsers) {
            sessions.remove(userId);
        }
    }

    public void sendToUser(String chatId, String userId, String message) {
        Map<String, Session> sessions = chatSessions.get(chatId);

        if (sessions != null) {
            Session session = sessions.get(userId);

            if (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("Error enviando mensaje a usuario " + userId + ": " + e.getMessage());
                }
            }
        }
    }

    public Set<String> getUsersInChat(String chatId) {
        Map<String, Session> sessions = chatSessions.get(chatId);
        return sessions != null ? new HashSet<>(sessions.keySet()) : new HashSet<>();
    }

    public boolean isUserConnected(String chatId, String userId) {
        Map<String, Session> sessions = chatSessions.get(chatId);
        return sessions != null && sessions.containsKey(userId);
    }

    public int getChatUserCount(String chatId) {
        Map<String, Session> sessions = chatSessions.get(chatId);
        return sessions != null ? sessions.size() : 0;
    }

    // Auxiliar
    private static class UserSession {
        final String chatId;
        final String userId;
        final Session session;

        UserSession(String chatId, String userId, Session session) {
            this.chatId = chatId;
            this.userId = userId;
            this.session = session;
        }
    }
}

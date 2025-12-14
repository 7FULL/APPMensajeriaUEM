package com.appmsg.appmensajeriauem;

import com.appmsg.appmensajeriauem.model.ChatMessage;
import com.appmsg.appmensajeriauem.repository.ChatMessageRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.bson.types.ObjectId;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@WebServlet(name = "messageServlet", value = "/api/messages")
public class MessageServlet extends HttpServlet {

    private ChatMessageRepository repo;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        repo = new ChatMessageRepository(new MongoDbClient());
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String chatId = req.getParameter("chatId");
        String limitStr = req.getParameter("limit");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (chatId == null || chatId.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Falta el parámetro chatId\"}");
            return;
        }

        try {
            ObjectId objectId = new ObjectId(chatId);
            List<ChatMessage> messages;

            if (limitStr != null && !limitStr.isEmpty()) {
                int limit = Integer.parseInt(limitStr);
                messages = repo.getMessages(objectId, limit);
            } else {
                messages = repo.getAllMessages(objectId);
            }

            String json = gson.toJson(messages);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(json);

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID de chat o límite inválido\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        try {
            JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);

            if (!json.has("chatId") || !json.has("senderId") || !json.has("message")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Faltan campos requeridos: chatId, senderId, message\"}");
                return;
            }

            ObjectId chatId = new ObjectId(json.get("chatId").getAsString());
            ObjectId senderId = new ObjectId(json.get("senderId").getAsString());
            ObjectId messageId = new ObjectId();
            String message = json.get("message").getAsString();

            String[] multimedia = new String[0];
            if (json.has("multimedia") && json.get("multimedia").isJsonArray()) {
                multimedia = gson.fromJson(json.get("multimedia"), String[].class);
            }

            String status = json.has("status") ? json.get("status").getAsString() : "sent";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Boolean updated = json.has("updated") ? json.get("updated").getAsBoolean() : false;

            ChatMessage chatMessage = new ChatMessage(
                    chatId, senderId, messageId, message,
                    multimedia, status, timestamp, updated
            );

            repo.sendMessage(chatMessage);

            String responseJson = gson.toJson(chatMessage);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(responseJson);

        } catch (JsonSyntaxException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"JSON inválido\"}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Error interno del servidor\"}");
        }
    }
}

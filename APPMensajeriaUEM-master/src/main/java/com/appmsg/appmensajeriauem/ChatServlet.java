package com.appmsg.appmensajeriauem;

import com.appmsg.appmensajeriauem.model.Chat;
import com.appmsg.appmensajeriauem.repository.ChatRepository;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "chatServlet", value = "/api/chat")
public class ChatServlet extends HttpServlet {

    private ChatRepository repo;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        repo = new ChatRepository(new MongoDbClient());
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String chatId = req.getParameter("chatId");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (chatId == null || chatId.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Falta el parámetro chatId\"}");
            return;
        }

        try {
            ObjectId objectId = new ObjectId(chatId);
            Chat chat = repo.enterConversation(objectId);

            if (chat == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Chat no encontrado\"}");
                return;
            }

            String json = gson.toJson(chat);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(json);

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID de chat inválido\"}");
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
            String action = json.has("action") ? json.get("action").getAsString() : null;

            if (action == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Falta el parámetro 'action'\"}");
                return;
            }

            switch (action) {
                case "startPrivateConversation":
                    handleStartPrivateConversation(json, resp);
                    break;

                case "createGroup":
                    handleCreateGroup(json, resp);
                    break;

                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Acción no válida\"}");
            }

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

    private void handleStartPrivateConversation(JsonObject json, HttpServletResponse resp)
            throws IOException {

        if (!json.has("user1Id") || !json.has("user2Id")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Faltan user1Id o user2Id\"}");
            return;
        }

        String user1IdStr = json.get("user1Id").getAsString();
        String user2IdStr = json.get("user2Id").getAsString();

        ObjectId user1Id = new ObjectId(user1IdStr);
        ObjectId user2Id = new ObjectId(user2IdStr);

        Chat chat = repo.startPrivateConversation(user1Id, user2Id);

        String responseJson = gson.toJson(chat);
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write(responseJson);
    }

    private void handleCreateGroup(JsonObject json, HttpServletResponse resp)
            throws IOException {

        if (!json.has("groupName") || !json.has("userIds")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Faltan groupName o userIds\"}");
            return;
        }

        String groupName = json.get("groupName").getAsString();
        String groupImage = json.has("groupImage") ? json.get("groupImage").getAsString() : null;

        List<ObjectId> userIds = new ArrayList<>();
        json.getAsJsonArray("userIds").forEach(element -> {
            userIds.add(new ObjectId(element.getAsString()));
        });

        Chat group = repo.createGroup(groupName, userIds, groupImage);

        String responseJson = gson.toJson(group);
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write(responseJson);
    }
}

package com.appmsg.appmensajeriauem;

import com.appmsg.appmensajeriauem.model.Chat;
import com.appmsg.appmensajeriauem.model.InviteLink;
import com.appmsg.appmensajeriauem.repository.ChatRepository;
import com.appmsg.appmensajeriauem.repository.InviteLinkRepository;
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

@WebServlet(name = "inviteLinkServlet", value = "/api/invite")
public class InviteLinkServlet extends HttpServlet {

    private InviteLinkRepository inviteRepo;
    private ChatRepository chatRepo;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        MongoDbClient mongoClient = new MongoDbClient();
        inviteRepo = new InviteLinkRepository(mongoClient);
        chatRepo = new ChatRepository(mongoClient);
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String inviteCode = req.getParameter("code");

        if (inviteCode == null || inviteCode.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Falta el parámetro 'code'\"}");
            return;
        }

        try {
            InviteLink inviteLink = inviteRepo.getInviteLinkByCode(inviteCode);

            if (inviteLink == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Enlace de invitación no encontrado\"}");
                return;
            }

            Chat chat = chatRepo.getChatById(inviteLink.getChatId());

            if (chat == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Chat no encontrado\"}");
                return;
            }

            JsonObject response = new JsonObject();
            response.addProperty("inviteCode", inviteLink.getInviteCode());
            response.addProperty("chatId", inviteLink.getChatId().toString());
            response.addProperty("chatName", chat.getChatName() != null ? chat.getChatName() : "Chat privado");
            response.addProperty("chatImage", chat.getChatImage());
            response.addProperty("memberCount", chat.getUserList() != null ? chat.getUserList().size() : 0);
            response.addProperty("createdBy", inviteLink.getCreatedBy().toString());
            response.addProperty("expiresAt", inviteLink.getExpiresAt() != null ? inviteLink.getExpiresAt().getTime() : null);
            response.addProperty("maxUses", inviteLink.getMaxUses());
            response.addProperty("currentUses", inviteLink.getCurrentUses());
            response.addProperty("isValid", inviteLink.isValid());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Error interno del servidor\"}");
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
                case "create":
                    handleCreateInvite(json, resp, req);
                    break;

                case "join":
                    handleJoinViaInvite(json, resp);
                    break;

                case "deactivate":
                    handleDeactivateInvite(json, resp);
                    break;

                case "delete":
                    handleDeleteInvite(json, resp);
                    break;

                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Acción no válida\"}");
            }

        } catch (JsonSyntaxException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"JSON inválido\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Error interno del servidor\"}");
        }
    }

    private void handleCreateInvite(JsonObject json, HttpServletResponse resp, HttpServletRequest req) throws IOException {
        if (!json.has("chatId") || !json.has("createdBy")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Faltan chatId o createdBy\"}");
            return;
        }

        ObjectId chatId = new ObjectId(json.get("chatId").getAsString());
        ObjectId createdBy = new ObjectId(json.get("createdBy").getAsString());

        Integer expirationHours = json.has("expirationHours") ? json.get("expirationHours").getAsInt() : null;
        Integer maxUses = json.has("maxUses") ? json.get("maxUses").getAsInt() : null;

        Chat chat = chatRepo.getChatById(chatId);
        if (chat == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Chat no encontrado\"}");
            return;
        }

        if (!chat.getUserList().contains(createdBy)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\":\"El usuario no es miembro del chat\"}");
            return;
        }

        InviteLink inviteLink = inviteRepo.createInviteLink(chatId, createdBy, expirationHours, maxUses);

        String baseUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.getContextPath();
        String inviteUrl = baseUrl + "/join?code=" + inviteLink.getInviteCode();

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("inviteCode", inviteLink.getInviteCode());
        response.addProperty("inviteUrl", inviteUrl);
        response.addProperty("chatId", chatId.toString());
        response.addProperty("expiresAt", inviteLink.getExpiresAt() != null ? inviteLink.getExpiresAt().getTime() : null);
        response.addProperty("maxUses", inviteLink.getMaxUses());

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.getWriter().write(gson.toJson(response));
    }

    private void handleJoinViaInvite(JsonObject json, HttpServletResponse resp) throws IOException {
        if (!json.has("inviteCode") || !json.has("userId")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Faltan inviteCode o userId\"}");
            return;
        }

        String inviteCode = json.get("inviteCode").getAsString();
        ObjectId userId = new ObjectId(json.get("userId").getAsString());

        InviteLink inviteLink = inviteRepo.getInviteLinkByCode(inviteCode);

        if (inviteLink == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Enlace de invitación no encontrado\"}");
            return;
        }

        if (!inviteLink.isValid()) {
            String reason = "Enlace inválido";
            if (!inviteLink.getActive()) {
                reason = "El enlace ha sido desactivado";
            } else if (inviteLink.getExpiresAt() != null && System.currentTimeMillis() > inviteLink.getExpiresAt().getTime()) {
                reason = "El enlace ha expirado";
            } else if (inviteLink.getMaxUses() != null && inviteLink.getCurrentUses() >= inviteLink.getMaxUses()) {
                reason = "El enlace ha alcanzado el máximo de usos";
            }

            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + reason + "\"}");
            return;
        }

        Chat chat = chatRepo.getChatById(inviteLink.getChatId());

        if (chat == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Chat no encontrado\"}");
            return;
        }

        if (chat.getUserList().contains(userId)) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\":\"Ya eres miembro de este chat\",\"chatId\":\"" + chat.getId().toString() + "\"}");
            return;
        }

        chatRepo.addUserToChat(inviteLink.getChatId(), userId);

        inviteRepo.incrementUses(inviteCode);

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.addProperty("message", "Te has unido al chat exitosamente");
        response.addProperty("chatId", chat.getId().toString());
        response.addProperty("chatName", chat.getChatName() != null ? chat.getChatName() : "Chat privado");

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(response));
    }

    private void handleDeactivateInvite(JsonObject json, HttpServletResponse resp) throws IOException {
        if (!json.has("inviteCode")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Falta inviteCode\"}");
            return;
        }

        String inviteCode = json.get("inviteCode").getAsString();
        inviteRepo.deactivateInviteLink(inviteCode);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("{\"success\":true,\"message\":\"Enlace desactivado\"}");
    }

    private void handleDeleteInvite(JsonObject json, HttpServletResponse resp) throws IOException {
        if (!json.has("inviteCode")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Falta inviteCode\"}");
            return;
        }

        String inviteCode = json.get("inviteCode").getAsString();
        inviteRepo.deleteInviteLink(inviteCode);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("{\"success\":true,\"message\":\"Enlace eliminado\"}");
    }
}

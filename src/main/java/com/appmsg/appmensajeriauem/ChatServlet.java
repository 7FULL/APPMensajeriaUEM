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

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String chatId = req.getParameter("chatId");

        //Check that the chat id is not null ot empty
        if (chatId == null || chatId.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: doGet, Error: The parameter chatId is null or empty\"}");
            return;
        }

        try {

            //Try to access the chat by its id
            ObjectId objectId = new ObjectId(chatId);
            Chat chat = repo.enterChat(objectId);

            //check if the chat wasn't found
            if (chat == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: doGet, Error: The chat was not found\"}");
                return;
            }

            String json = gson.toJson(chat);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(json);

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: doGet, Error: The chatId was invalid\"}");
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
                resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: doPost, Error: Theparameter 'action' is missing\"}");
                return;
            }

            switch (action) {
                case "createChat":
                    handleCreateChat(json, resp);
                    break;

                case "deleteChat":
                    handleDeleteChat(json, resp);
                    break;

                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: doPost, Error: The value of 'action'{" + action + "} is invalid\"}");
            }

        } catch (JsonSyntaxException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: doPost, Error: Invalid JSON\"}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: doPost, Error: Internal server error\"}");
        }
    }

    private void handleCreateChat(JsonObject json, HttpServletResponse resp)
            throws IOException {

        //Check that the required parameters are present at the JSON
        if (!json.has("chatName")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: handleCreateChat, Error: the parameter chatName is missing from the JSON and is required to create a chat\"}");
            return;
        }

        if (!json.has("userList")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: handleCreateChat, Error: the parameter userList is missing from the JSON and is required to create a chat\"}");
            return;
        }

        if (!json.has("chatImage")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: handleCreateChat, Error: the parameter chatImage is missing from the JSON and is required to create a chat\"}");
            return;
        }

        //Extract data from JSON to send to the repository
        String chatName = json.has("chatName") ? json.get("chatName").getAsString() : null;
        List<ObjectId> userList = json.has("userList") ? gson.fromJson(json.get("userList").toString(), List.class) : null;
        String chatImage = json.has("chatImage") ? json.get("chatImage").getAsString() : null;

        //Create the new chat
        Chat chat = repo.createChat(chatName,userList,chatImage);

        //Return the chat as JSON
        String responseJson = gson.toJson(chat);

        //Set the status code to 201 Created
        resp.setStatus(HttpServletResponse.SC_CREATED);

        //Write the JSON response
        resp.getWriter().write(responseJson);
    }

    private void handleDeleteChat(JsonObject json, HttpServletResponse resp)
            throws IOException {

        //Check that the required parameters are present at the JSON
        if (!json.has("chatId")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Class: ChatServlet, Method: handleCreateChat, Error: the parameter chatId is missing from the JSON and is required to create a chat\"}");
            return;
        }

        //Extract data from JSON to send to the repository
        String chatIdStr = json.has("chatId") ? json.get("chatId").getAsString() : null;
        ObjectId chatId = new ObjectId(chatIdStr);

        //Delete the specified chat
        if(repo.deleteChat(chatId)){
            //Set the status code to 200 OK because the chat was deleted successfully
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }
}

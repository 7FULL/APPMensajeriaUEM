package com.appmsg.appmensajeriauem;

import com.appmsg.appmensajeriauem.model.User;
import com.appmsg.appmensajeriauem.repository.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bson.types.ObjectId;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "profileServlet", value = "/api/profile")
public class ProfileServlet extends HttpServlet {

    private UserRepository repo;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        repo = new UserRepository(new MongoDbClient());
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String userId = req.getParameter("userId");
        String username = req.getParameter("username");
        String email = req.getParameter("email");

        User user = null;

        try {
            if (userId != null && !userId.isEmpty()) {
                ObjectId objectId = new ObjectId(userId);
                user = repo.getUserById(objectId);
            } else if (username != null && !username.isEmpty()) {
                user = repo.getUserByUsername(username);
            } else if (email != null && !email.isEmpty()) {
                user = repo.getUserByEmail(email);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Debe proporcionar userId, username o email\"}");
                return;
            }

            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Usuario no encontrado\"}");
                return;
            }

            JsonObject profile = createPublicProfile(user);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(profile));

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID de usuario inválido\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Error interno del servidor\"}");
        }
    }

    private JsonObject createPublicProfile(User user) {
        JsonObject profile = new JsonObject();

        profile.addProperty("userId", user.getId().toString());
        profile.addProperty("username", user.getUsername());
        profile.addProperty("email", user.getEmail());
        profile.addProperty("picture", user.getPicture());
        profile.addProperty("status", user.getStatus());
        profile.addProperty("wallpaper", user.getWallpaper());

        return profile;
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject info = new JsonObject();
        info.addProperty("service", "Profile Service");
        info.addProperty("endpoint", "/api/profile");
        info.addProperty("method", "GET");
        info.addProperty("parameters", "userId OR username OR email");
        info.addProperty("description", "Obtiene el perfil público de un usuario");

        resp.getWriter().write(info.toString());
    }
}

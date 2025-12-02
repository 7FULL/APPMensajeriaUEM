package com.appmsg.appmensajeriauem;

import com.appmsg.appmensajeriauem.model.UserSettings;
import com.appmsg.appmensajeriauem.repository.SettingsRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.bson.types.ObjectId;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(name = "settingsServlet", value = "/api/settings")
public class SettingsServlet extends HttpServlet {

    private SettingsRepository repo;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        repo = new SettingsRepository(new MongoDbClient());
        gson = new Gson();
    }

    // GET /api/settings?userId=xxxxx
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String userIdParam = req.getParameter("userId");

        if (userIdParam == null || userIdParam.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Falta el parámetro userId\"}");
            return;
        }

        try {
            ObjectId userId = new ObjectId(userIdParam);
            UserSettings settings = repo.getByUserId(userId);

            if (settings == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Ajustes no encontrados\"}");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(settings));

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID inválido\"}");
        }
    }

    // POST /api/settings  { userId, darkMode, wallpaperPath, displayName, status }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);

        try {
            JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);

            if (!json.has("userId")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Falta userId\"}");
                return;
            }

            ObjectId userId = new ObjectId(json.get("userId").getAsString());

            UserSettings existing = repo.getByUserId(userId);

            UserSettings settings = existing != null ? existing : new UserSettings();
            settings.setUserId(userId);
            settings.setDarkMode(json.has("darkMode") && json.get("darkMode").getAsBoolean());
            settings.setWallpaperPath(json.has("wallpaperPath") ? json.get("wallpaperPath").getAsString() : null);
            settings.setDisplayName(json.has("displayName") ? json.get("displayName").getAsString() : null);
            settings.setStatus(json.has("status") ? json.get("status").getAsString() : null);

            repo.save(settings);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(settings));

        } catch (JsonSyntaxException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"JSON inválido\"}");
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID inválido\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Error interno del servidor\"}");
        }
    }
}

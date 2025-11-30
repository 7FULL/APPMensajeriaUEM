package com.appmsg.appmensajeriauem;

import com.appmsg.appmensajeriauem.model.UserSettings;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(name = "settingsServlet", value = "/api/settings")
public class SettingsServlet extends HttpServlet {

    // ObjectMapper nos permite convertir objetos Java a JSON
    private final ObjectMapper mapper = new ObjectMapper();

    // Aquí guardamos los ajustes "actuales" en memoria
    private UserSettings currentSettings;

    @Override
    public void init() {
        System.out.println("SettingsServlet inicializado.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Creamos un objeto UserSettings de prueba
        UserSettings settings = new UserSettings();
        settings.setUserId("demo-user");
        settings.setDarkMode(true);
        settings.setWallpaperPath(null);
        settings.setDisplayName("Usuario Demo");
        settings.setStatus("Disponible");

        // Indicamos que la respuesta será JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Convertimos el objeto a JSON y lo enviamos
        mapper.writeValue(response.getWriter(), settings);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            // Leemos el JSON del cuerpo y lo convertimos a UserSettings
            UserSettings incoming = mapper.readValue(request.getInputStream(), UserSettings.class);

            // (más adelante vendrá del login o de un token)
            incoming.setUserId("demo-user");

            // Guardamos en memoria como "ajustes actuales"
            this.currentSettings = incoming;

            // Devolvemos 204 No Content para indicar que se ha guardado correctamente
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (Exception e) {
            // Si el JSON viene mal formado o hay algún problema,
            // devolvemos un 400 Bad Request
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JSON inválido o datos incorrectos");
        }
    }

    @Override
    public void destroy() {
        System.out.println("SettingsServlet destruido.");
    }
}


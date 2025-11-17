package com.appmsg.appmensajeriauem.model;

/**
 * Representa los ajustes configurables por un usuario
 * dentro de la aplicación.
 *
 * Esta clase será convertida a JSON cuando el cliente (JavaFX u otro)
 * solicite los ajustes, o envíe cambios para guardarlos.
 */
public class UserSettings {

    /**
     * Identificador del usuario al que pertenecen estos ajustes.
     * Más adelante vendrá del login; por ahora lo pasaremos por parámetro.
     */
    private String userId;

    /**
     * Indica si el usuario quiere utilizar el tema oscuro.
     * true  = tema oscuro
     * false = tema claro
     */
    private boolean darkMode;

    /**
     * Ruta (relativa o absoluta) al archivo de fondo de pantalla.
     * Puede ser una ruta local ("C:/fondos/fondo1.jpg")
     * o una URL ("http://servidor.com/imagen.png").
     * Si el usuario no ha configurado fondo, puede ser null.
     */
    private String wallpaperPath;

    /**
     * Nombre visible del usuario dentro de la aplicación.
     * No tiene por qué coincidir con su username/login.
     */
    private String displayName;

    /**
     * Texto de estado del usuario (ej.: "Disponible", "Ocupado", etc.)
     */
    private String status;

    // -----------------------------
    // Getters & Setters
    // -----------------------------

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public String getWallpaperPath() {
        return wallpaperPath;
    }

    public void setWallpaperPath(String wallpaperPath) {
        this.wallpaperPath = wallpaperPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

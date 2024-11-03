package com.smishguard;

public class AlertMessageModel {
    private String id;
    private String contenido;
    private String url;

    // Constructor
    public AlertMessageModel(String id, String contenido, String url) {
        this.id = id;
        this.contenido = contenido;
        this.url = url;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getContenido() {
        return contenido;
    }

    public String getUrl() {
        return url;
    }
}
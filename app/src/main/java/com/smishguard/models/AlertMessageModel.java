package com.smishguard.models;

public class AlertMessageModel {
    private String id;
    private String contenido;
    private String url;
    private String nivelPeligro;
    private String justificacionGpt;
    private int ponderado;

    // Constructor actualizado
    public AlertMessageModel(String id, String contenido, String url, String nivelPeligro, String justificacionGpt, int ponderado) {
        this.id = id;
        this.contenido = contenido;
        this.url = url;
        this.nivelPeligro = nivelPeligro;
        this.justificacionGpt = justificacionGpt;
        this.ponderado = ponderado;
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

    public String getNivelPeligro() {
        return nivelPeligro;
    }

    public String getJustificacionGpt() {
        return justificacionGpt;
    }

    public int getPonderado() {
        return ponderado;
    }
}
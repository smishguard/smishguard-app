package com.smishguard.models;

public class SmsModel {
    private String address;
    private String body;
    private long date;
    private String analisisSmishguard;
    private String analisisGpt;
    private String enlace;
    private int puntaje;
    private String fechaAnalisisFormatted; // Campo adicional para la fecha formateada

    // Constructor original
    public SmsModel(String address, String body, long date) {
        this.address = address;
        this.body = body;
        this.date = date;
    }

    // Constructor original
    public SmsModel(String address, String body, long date, String analisisSmishguard, String analisisGpt, String enlace, int puntaje) {
        this.address = address;
        this.body = body;
        this.date = date;
        this.analisisSmishguard = analisisSmishguard;
        this.analisisGpt = analisisGpt;
        this.enlace = enlace;
        this.puntaje = puntaje;
    }

    // Constructor adicional para incluir fecha formateada
    public SmsModel(String address, String body, long date, String analisisSmishguard, String analisisGpt, String enlace, int puntaje, String fechaAnalisisFormatted) {
        this.address = address;
        this.body = body;
        this.date = date;
        this.analisisSmishguard = analisisSmishguard;
        this.analisisGpt = analisisGpt;
        this.enlace = enlace;
        this.puntaje = puntaje;
        this.fechaAnalisisFormatted = fechaAnalisisFormatted;
    }

    // Getters y Setters

    public String getAddress() {
        return address;
    }

    public String getBody() {
        return body;
    }

    public long getDate() {
        return date;
    }

    public String getAnalisisSmishguard() {
        return analisisSmishguard;
    }

    public String getAnalisisGpt() {
        return analisisGpt;
    }

    public String getEnlace() {
        return enlace;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public String getFechaAnalisisFormatted() {
        return fechaAnalisisFormatted;
    }

    public void setFechaAnalisisFormatted(String fechaAnalisisFormatted) {
        this.fechaAnalisisFormatted = fechaAnalisisFormatted;
    }
}
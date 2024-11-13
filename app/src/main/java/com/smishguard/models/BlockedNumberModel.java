package com.smishguard.models;

public class BlockedNumberModel {
    private String number;
    private String fechaBloqueo;

    public BlockedNumberModel(String number, String fechaBloqueo) {
        this.number = number;
        this.fechaBloqueo = fechaBloqueo;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getFechaBloqueo() {
        return fechaBloqueo;
    }

    public void setFechaBloqueo(String fechaBloqueo) {
        this.fechaBloqueo = fechaBloqueo;
    }
}
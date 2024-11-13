package com.smishguard.models;

public class SupportCommentModel {
    private String id;
    private String comentario;
    private String correo;
    private String fecha;

    public SupportCommentModel(String id, String comentario, String correo, String fecha) {
        this.id = id;
        this.comentario = comentario;
        this.correo = correo;
        this.fecha = fecha;
    }

    public String getId() {
        return id;
    }

    public String getComentario() {
        return comentario;
    }

    public String getCorreo() {
        return correo;
    }

    public String getFecha() {
        return fecha;
    }
}
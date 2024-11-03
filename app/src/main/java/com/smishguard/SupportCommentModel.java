package com.smishguard;

public class SupportCommentModel {
    private String comentario;
    private String correo;
    private String fecha;

    public SupportCommentModel(String comentario, String correo, String fecha) {
        this.comentario = comentario;
        this.correo = correo;
        this.fecha = fecha;
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
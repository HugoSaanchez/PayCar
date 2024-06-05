package com.example.demo.entityDTO;

public class ComentarioDTO {
    private String comentario;
    private String pasajeroNombre;

    public ComentarioDTO(String comentario, String pasajeroNombre) {
        this.comentario = comentario;
        this.pasajeroNombre = pasajeroNombre;
    }

    // Getters and Setters
    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getPasajeroNombre() {
        return pasajeroNombre;
    }

    public void setPasajeroNombre(String pasajeroNombre) {
        this.pasajeroNombre = pasajeroNombre;
    }
}

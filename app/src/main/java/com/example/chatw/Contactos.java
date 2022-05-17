package com.example.chatw;

public class Contactos {
    private String nombre, ciudad, estado, imagen;
    public Contactos(){
    }

    public Contactos(String nombre, String ciudad, String estado, String imagen) {
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.estado = estado;
        this.imagen = imagen;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getCiudad() {
        return ciudad;
    }
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }
    public String getImagen() {
        return imagen;
    }
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}

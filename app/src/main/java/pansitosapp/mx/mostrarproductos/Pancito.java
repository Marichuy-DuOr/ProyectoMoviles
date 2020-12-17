package pansitosapp.mx.mostrarproductos;

import java.io.Serializable;

public class Pancito implements Serializable {
    Integer id;
    String nombre;
    Float precio;
    String imagen;
    String descripcion;

    public Pancito(Integer id, String nombre, Float precio, String imagen, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
        this.descripcion = descripcion;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Float getPrecio() {
        return precio;
    }

    public String getImagen() {
        return imagen;
    }

    public String getDescripcion() {
        return descripcion;
    }
}


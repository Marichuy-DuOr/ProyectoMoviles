package pansitosapp.mx.carrito;

import java.io.Serializable;

public class Carrito implements Serializable {
    Integer id;
    Integer id_producto;
    String nombre;
    Float precio;
    String imagen;
    String descripcion;
    Integer cantidad;

    public Carrito(Integer id, Integer id_producto, String nombre, Float precio, String imagen, String descripcion, Integer cantidad) {
        this.id = id;
        this.id_producto = id_producto;
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
    }

    public Integer getId() {
        return id;
    }

    public void setIdc(Integer id) {
        this.id = id;
    }

    public Integer getId_producto() {
        return id_producto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Float getPrecio() {
        return precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
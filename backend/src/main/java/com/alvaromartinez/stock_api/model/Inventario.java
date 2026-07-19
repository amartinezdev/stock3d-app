package com.alvaromartinez.stock_api.model;

import jakarta.persistence.*;

/**
 * Rollos CERRADOS que tiene cada usuario de cada producto - el stock aquí
 * es siempre por usuario, no global. Al abrir un rollo se resta 1 aquí y
 * nace una fila en EnUso. La UNIQUE compuesta (usuario_id, producto_id)
 * impide dos filas del mismo par: la cantidad vive siempre en UNA fila.
 */
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "producto_id"}))
@Entity
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne: muchas filas de Inventario pueden ser del mismo usuario.
    @ManyToOne
    private Usuario usuario;

    // @ManyToOne: y también del mismo producto (una por usuario que lo tenga).
    @ManyToOne
    private Producto producto;

    // int, no BigDecimal: se cuenta en rollos enteros, no en gramos.
    private int cantidad;

    // Constructor vacío obligatorio para Hibernate.
    public Inventario() {
    }

    // Constructor que usa InventarioService al crear la fila de un usuario+producto.
    public Inventario(Long id, Usuario usuario, Producto producto, int cantidad) {
        this.id = id;
        this.usuario = usuario;
        this.producto = producto;
        this.cantidad = cantidad;
    }

    // Getters/setters: Hibernate los usa al convertir entre fila y objeto.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}

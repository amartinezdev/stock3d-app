package com.alvaromartinez.stock_api.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Entidad JPA: el CATÁLOGO compartido de productos (nombre, precio de
 * referencia, categoría, peso por rollo). El stock real NO vive aquí:
 * cada usuario lleva el suyo en Inventario/EnUso.
 */
@Entity
public class Producto {

    // Clave primaria autoincrement: la genera la BBDD al guardar (por eso va null al crear).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio; // BigDecimal, no double/float: evita errores de redondeo con dinero

    // Se guarda como texto ("PLA"), no como posición numérica del enum.
    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    // Gramos de UN rollo sin abrir (dato fijo del catálogo). Se usa para
    // inicializar EnUso.gramosRestantes al abrir un rollo.
    private BigDecimal pesoRollo;

    // Constructor que usa ProductoController para montar la entidad desde el DTO.
    public Producto(Long id, String nombre, String descripcion, BigDecimal precio, Categoria categoria, BigDecimal pesoRollo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.pesoRollo = pesoRollo;

    }

    // Constructor vacío obligatorio para Hibernate (crea el objeto por
    // reflexión y rellena los campos leyendo la fila).
    public Producto() {

    }

    // Getters/setters: Hibernate los usa al convertir entre fila y objeto.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPesoRollo() {
        return pesoRollo;
    }

    public void setPesoRollo(BigDecimal pesoRollo) {
        this.pesoRollo = pesoRollo;
    }
}



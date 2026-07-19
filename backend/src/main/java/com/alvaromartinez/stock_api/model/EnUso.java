package com.alvaromartinez.stock_api.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un rollo YA ABIERTO que se va gastando (estado mutable, al contrario que
 * Movimiento, que es histórico inmutable). No hay campo "agotado": se
 * consulta gramosRestantes <= 0, para no tener el mismo dato en dos sitios
 * que se puedan desincronizar. Tampoco hay UNIQUE (usuario, producto) como
 * en Inventario: puede haber varios rollos abiertos del mismo producto.
 */
@Entity
public class EnUso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne: un usuario puede tener varios rollos abiertos a la vez.
    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Producto producto;

    // BigDecimal (no int): los gramos pueden tener decimales. Al abrir el
    // rollo se inicializa con Producto.pesoRollo.
    private BigDecimal gramosRestantes;

    // Solo el día (sin hora): no hace falta más precisión aquí.
    private LocalDate fechaApertura;

    // Constructor que usa InventarioService.abrirRollo al crear el rollo abierto.
    public EnUso(Long id, Usuario usuario, Producto producto, BigDecimal gramosRestantes, LocalDate fechaApertura) {
        this.id = id;
        this.usuario = usuario;
        this.producto = producto;
        this.gramosRestantes = gramosRestantes;
        this.fechaApertura = fechaApertura;
    }

    // Constructor vacío obligatorio para Hibernate.
    public EnUso() {
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

    public BigDecimal getGramosRestantes() {
        return gramosRestantes;
    }

    public void setGramosRestantes(BigDecimal gramosRestantes) {
        this.gramosRestantes = gramosRestantes;
    }

    public LocalDate getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDate fechaApertura) {
        this.fechaApertura = fechaApertura;
    }
}

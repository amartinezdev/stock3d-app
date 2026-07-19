package com.alvaromartinez.stock_api.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Histórico INMUTABLE de stock: cada fila es un hecho que ya pasó y no se
 * edita nunca (al contrario que Inventario/EnUso). Sirve para auditoría
 * (quién y cuándo) y estadísticas. Ojo: abrir un rollo NO genera Movimiento
 * - no es entrada ni salida real, solo cambia de "cerrado" a "abierto".
 */
@Entity
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Guardado como texto ("ENTRADA"...), igual que Categoria en Producto.
    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    // @ManyToOne: muchos movimientos del mismo usuario / mismo producto.
    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Producto producto;

    // BigDecimal, no int: puede ser rollos enteros (entrada/venta) o gramos
    // con decimales (consumo).
    private BigDecimal cantidad;

    // Precio real de ESTA transacción (puede diferir del de referencia del
    // catálogo). null si no es una venta.
    private BigDecimal precio;

    // Con hora (no solo día): permite ordenar movimientos del mismo día.
    private LocalDateTime fecha;

    // Campo libre opcional.
    private String notas;

    // Constructor vacío obligatorio para Hibernate.
    public Movimiento() {

    }

    // Constructor que usa InventarioService al registrar cada operación.
    public Movimiento(Long id, TipoMovimiento tipo, Usuario usuario, Producto producto, BigDecimal cantidad, BigDecimal precio, LocalDateTime fecha, String notas) {
        this.id = id;
        this.tipo = tipo;
        this.usuario = usuario;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
        this.fecha = fecha;
        this.notas = notas;
    }

    // Getters/setters: Hibernate los usa al convertir entre fila y objeto.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
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

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}

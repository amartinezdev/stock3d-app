package com.alvaromartinez.stock_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de entrada de venta. Nunca lleva Usuario ni Producto completos: solo
 * el id del producto, el resto lo resuelve el servidor.
 *
 * @param productoId id del producto que se vende (rollos enteros, sin abrir).
 * @param cantidad   rollos que se venden.
 * @param precio     precio unitario real de ESTA venta; puede diferir del
 *                   precio de referencia del catálogo.
 */
public record VentaDTO(@NotNull Long productoId, @Positive int cantidad,
                       @Positive BigDecimal precio) {
}

package com.alvaromartinez.stock_api.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO de entrada para InventarioController.entrada - el usuario que hace la
 * entrada no viaja aquí (sale de Authentication), solo qué producto y
 * cuántos rollos.
 *
 * @param productoId id del producto del catálogo al que se le añade stock.
 * @param cantidad   rollos cerrados que se añaden (siempre positivo: una
 *                   entrada de 0 no tendría sentido).
 */
public record EntradaStockDTO(@NotNull Long productoId, @Positive int cantidad) {
}

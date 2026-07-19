package com.alvaromartinez.stock_api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de entrada para InventarioController.abrirRollo. Un único campo:
 * el usuario sale de Authentication, la cantidad (siempre 1 rollo por
 * llamada), los gramos iniciales y la fecha de apertura los calcula solo
 * el Service - lo único que decide el cliente es de qué producto.
 *
 * @param productoId id del producto del que se quiere abrir un rollo.
 */
public record AbrirRolloDTO(@NotNull Long productoId) {
}

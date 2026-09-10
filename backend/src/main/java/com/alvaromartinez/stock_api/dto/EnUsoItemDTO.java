package com.alvaromartinez.stock_api.dto;

import com.alvaromartinez.stock_api.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de salida de un rollo abierto propio (GET /inventario/enUso).
 * Igual que InventarioItemDTO, no lleva usuario: siempre es el autenticado.
 *
 * @param id              id del EnUso; es el que hay que mandar en
 *                        POST /inventario/consumir.
 * @param producto        producto de ese rollo.
 * @param gramosRestantes gramos que quedan en el rollo.
 * @param fechaApertura   día en que se abrió.
 */
public record EnUsoItemDTO(Long id, Producto producto, BigDecimal gramosRestantes, LocalDate fechaApertura) {
}

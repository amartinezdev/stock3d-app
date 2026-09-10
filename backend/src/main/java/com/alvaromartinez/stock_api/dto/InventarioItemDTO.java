package com.alvaromartinez.stock_api.dto;

import com.alvaromartinez.stock_api.model.Producto;

/**
 * DTO de salida de una fila del inventario propio (GET /inventario).
 * No lleva usuario: el listado SIEMPRE es el del usuario autenticado, así
 * que repetir sus datos en cada fila solo engordaría la respuesta.
 *
 * @param id       id de la fila de Inventario.
 * @param producto producto del catálogo al que corresponde la fila.
 * @param cantidad rollos cerrados que tiene ahora mismo.
 */
public record InventarioItemDTO(Long id, Producto producto, int cantidad) {
}

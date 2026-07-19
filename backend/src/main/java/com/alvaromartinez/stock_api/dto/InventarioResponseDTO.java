package com.alvaromartinez.stock_api.dto;

import com.alvaromartinez.stock_api.model.Producto;

/**
 * DTO de salida con el estado de una fila de Inventario (entrada).
 * UsuarioResponseDTO en vez de Usuario para no filtrar la contraseña;
 * Producto va completo porque no tiene nada sensible.
 *
 * @param id       id de la fila de Inventario.
 * @param usuario  dueño de ese inventario, sin datos sensibles.
 * @param producto producto del catálogo al que corresponde la fila.
 * @param cantidad rollos cerrados YA actualizados tras la operación.
 */
public record InventarioResponseDTO(Long id, UsuarioResponseDTO usuario, Producto producto,
                                    int cantidad) {
}

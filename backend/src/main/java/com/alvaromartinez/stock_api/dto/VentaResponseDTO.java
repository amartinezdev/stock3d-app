package com.alvaromartinez.stock_api.dto;

import com.alvaromartinez.stock_api.model.Producto;

import java.math.BigDecimal;

/**
 * DTO de salida de InventarioController.venta. UsuarioResponseDTO en vez de
 * Usuario, para no filtrar la contraseña hasheada.
 *
 * @param usuario  usuario que ha vendido, sin datos sensibles.
 * @param producto producto vendido.
 * @param cantidad rollos que quedan en Inventario tras la venta (dato real,
 *                 no la cantidad vendida).
 * @param precio   precio unitario al que se ha vendido en esta transacción.
 */
public record VentaResponseDTO(UsuarioResponseDTO usuario, Producto producto, int cantidad, BigDecimal precio) {
}

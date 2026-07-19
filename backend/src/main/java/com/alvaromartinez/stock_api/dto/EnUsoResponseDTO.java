package com.alvaromartinez.stock_api.dto;

import com.alvaromartinez.stock_api.model.Producto;

import java.math.BigDecimal;

/**
 * DTO de salida de InventarioController.consumir - refleja el EnUso tras
 * restar los gramos consumidos. UsuarioResponseDTO en vez de Usuario, para
 * no filtrar la contraseña hasheada.
 *
 * @param usuario         dueño del rollo, sin datos sensibles.
 * @param producto        producto de ese rollo.
 * @param gramosRestantes gramos que quedan DESPUÉS de este consumo.
 */
public record EnUsoResponseDTO(UsuarioResponseDTO usuario, Producto producto, BigDecimal gramosRestantes) {
}

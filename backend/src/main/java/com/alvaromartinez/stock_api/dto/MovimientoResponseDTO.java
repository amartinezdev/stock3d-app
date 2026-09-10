package com.alvaromartinez.stock_api.dto;

import com.alvaromartinez.stock_api.model.Producto;
import com.alvaromartinez.stock_api.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida de una fila del histórico (GET /movimientos). Mismos campos
 * que la entidad Movimiento menos el usuario, que siempre es el autenticado.
 *
 * @param id       id del movimiento.
 * @param tipo     ENTRADA, SALIDA_VENTA o SALIDA_USO.
 * @param producto producto afectado.
 * @param cantidad rollos (entrada/venta) o gramos (consumo).
 * @param precio   precio unitario real; null si no es una venta.
 * @param fecha    fecha y hora del movimiento.
 * @param notas    campo libre, puede ser null.
 */
public record MovimientoResponseDTO(Long id, TipoMovimiento tipo, Producto producto, BigDecimal cantidad,
                                    BigDecimal precio, LocalDateTime fecha, String notas) {
}

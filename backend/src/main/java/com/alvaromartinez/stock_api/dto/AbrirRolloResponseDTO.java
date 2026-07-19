package com.alvaromartinez.stock_api.dto;

import com.alvaromartinez.stock_api.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de salida de InventarioController.abrirRollo - refleja el EnUso recién
 * creado. Mismos campos que la entidad EnUso, pero con UsuarioResponseDTO
 * en vez de Usuario (para no filtrar la contraseña hasheada).
 *
 * @param id               id del EnUso recién creado.
 * @param usuario          dueño del rollo abierto, sin datos sensibles.
 * @param producto         producto del que es este rollo.
 * @param gramosRestantes  gramos del rollo justo al abrirlo (el peso
 *                         completo, Producto.pesoRollo).
 * @param fechaApertura    día en que se abrió el rollo.
 */
public record AbrirRolloResponseDTO(Long id, UsuarioResponseDTO usuario, Producto producto, BigDecimal gramosRestantes,
                                    LocalDate fechaApertura) {
}

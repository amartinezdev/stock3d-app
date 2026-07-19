package com.alvaromartinez.stock_api.dto;

/**
 * DTO de salida del login. Sin validaciones: lo genera el servidor,
 * no es entrada del cliente.
 *
 * @param token el JWT completo que el cliente mandará después en la
 *              cabecera "Authorization: Bearer {token}".
 */
public record TokenResponseDTO(String token) {
}

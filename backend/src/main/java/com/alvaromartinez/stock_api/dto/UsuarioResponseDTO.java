package com.alvaromartinez.stock_api.dto;

import com.alvaromartinez.stock_api.model.Rol;

/**
 * DTO de salida con los datos públicos de un usuario. Existe sobre todo
 * para que el hash de la contraseña NUNCA salga en ninguna respuesta:
 * se usa en el registro y anidado en las respuestas de inventario.
 *
 * @param id       id generado por la BBDD.
 * @param nombre   nombre guardado.
 * @param email    email guardado.
 * @param userName nombre de usuario guardado.
 * @param rol      rol real asignado por el servidor, nunca el del cliente.
 */
public record UsuarioResponseDTO(Long id, String nombre, String email,
                                 String userName, Rol rol) {
}

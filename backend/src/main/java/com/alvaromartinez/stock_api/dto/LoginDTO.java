package com.alvaromartinez.stock_api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada del login: solo transporta lo que llega en el JSON.
 * Es un record: constructor y accesores (userName(), sin "get") se generan
 * solos, y es inmutable.
 *
 * @param userName nombre de usuario tal como lo escribe el cliente.
 * @param password en texto plano; se compara contra el hash guardado con
 *                 PasswordEncoder.matches, nunca se guarda tal cual.
 */
public record LoginDTO(@NotBlank String userName, @NotBlank String password) {
}

package com.alvaromartinez.stock_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada del registro. A propósito NO tiene campo "rol": lo fija
 * siempre el backend como USER, para que nadie se auto-asigne ADMIN desde
 * el JSON. @Email no rechaza null/vacío, por eso va junto a @NotBlank.
 *
 * @param nombre   nombre de la persona.
 * @param email    email, único (se comprueba antes de guardar).
 * @param userName nombre de usuario, único.
 * @param password en texto plano; se hashea con BCrypt antes de guardar.
 */
public record RegistroDTO(@NotBlank String nombre, @Email @NotBlank String email, @NotBlank String userName,
                          @NotBlank String password) {
}

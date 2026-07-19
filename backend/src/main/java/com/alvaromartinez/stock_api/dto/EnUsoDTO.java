package com.alvaromartinez.stock_api.dto;

import com.alvaromartinez.stock_api.model.Usuario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de entrada de consumir. Nunca lleva un Usuario: el autenticado se
 * resuelve vía Authentication y luego se comprueba que sea el dueño del
 * EnUso indicado.
 *
 * @param id     id del rollo abierto del que se consume (el cliente elige
 *               cuál, por si tiene varios abiertos).
 * @param gramos cantidad a restar de gramosRestantes.
 */
public record EnUsoDTO(@NotNull Long id, @Positive BigDecimal gramos) {
}

package com.alvaromartinez.stock_api.dto;

import com.alvaromartinez.stock_api.model.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * DTO de entrada para crear/actualizar un producto. No es la entidad: es el
 * contrato de la API, así el cliente no puede mandar un id ni campos que no
 * queramos exponer.
 *
 * @param nombre      nombre del producto.
 * @param descripcion descripción del producto.
 * @param precio      precio de referencia; @PositiveOrZero porque 0 es válido.
 * @param categoria   tipo de filamento (enum, @NotNull porque no es texto).
 * @param pesoRollo   gramos de un rollo sin abrir; @Positive porque un rollo
 *                    de 0 gramos no existe.
 */
public record ProductoDTO(@NotBlank String nombre, @NotBlank String descripcion, @PositiveOrZero BigDecimal precio,
                          @NotNull
                          Categoria categoria, @Positive BigDecimal pesoRollo) {

}


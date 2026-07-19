package com.alvaromartinez.stock_api.exception;

import java.time.LocalDateTime;

/**
 * Formato único de error de toda la API: GlobalExceptionHandler convierte
 * cualquier excepción capturada en uno de estos.
 *
 * @param mensaje   qué ha ido mal (mensaje de la excepción o validación).
 * @param codigo    código HTTP repetido en el cuerpo, por comodidad del cliente.
 * @param fechaHora cuándo ocurrió; útil para cruzar con los logs.
 */
public record ErrorResponse(String mensaje, int codigo, LocalDateTime fechaHora) {
}

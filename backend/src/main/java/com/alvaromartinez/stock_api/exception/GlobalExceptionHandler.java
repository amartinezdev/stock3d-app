package com.alvaromartinez.stock_api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Manejo de errores centralizado para TODA la API (@RestControllerAdvice).
 * Sin esto, cualquier excepción sin capturar acabaría en un 500 genérico
 * aunque el fallo fuera culpa del cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Convierte cualquier IllegalArgumentException de la app ("el usuario ya
     * existe", "no puedes vender más de lo que tienes"...) en un 400 con
     * mensaje claro, en vez de un 500 sin explicar.
     *
     * @param ex la excepción lanzada desde cualquier service/controller.
     * @return respuesta 400 con el formato común de ErrorResponse.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), 400, LocalDateTime.now());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Captura los fallos de @Valid en los DTOs (que lanzan
     * MethodArgumentNotValidException, no IllegalArgumentException).
     * Se saca el mensaje del primer campo que falló para no escribirlo a mano.
     *
     * @param ex la excepción con el detalle de qué campo(s) fallaron.
     * @return respuesta 400 con el formato común de ErrorResponse.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldError().getDefaultMessage();
        ErrorResponse error = new ErrorResponse(mensaje, 400, LocalDateTime.now());
        return ResponseEntity.badRequest().body(error);
    }

}

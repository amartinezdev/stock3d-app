package com.alvaromartinez.stock_api.model;

/**
 * Motivo de un Movimiento: no solo si entra o sale, también el porqué -
 * así luego podemos sacar estadísticas (vendido vs consumido).
 */
public enum TipoMovimiento {
    ENTRADA,       // entran rollos cerrados al Inventario
    SALIDA_VENTA,  // sale stock por venta (con precio)
    SALIDA_USO     // lo gasta el propio usuario (gramos de EnUso, sin precio)
}

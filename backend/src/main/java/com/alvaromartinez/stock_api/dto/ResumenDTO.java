package com.alvaromartinez.stock_api.dto;

import java.math.BigDecimal;

/**
 * DTO de salida de GET /inventario/resumen: las cifras agregadas que pinta
 * el panel principal del frontend. Se calcula al vuelo en InventarioService
 * a partir de Inventario, EnUso y Movimiento - no se guarda en ninguna
 * tabla, para que no pueda desincronizarse con los datos reales.
 *
 * @param rollosCerrados    total de rollos sin abrir del usuario.
 * @param rollosAbiertos    rollos abiertos que aún tienen gramos (> 0).
 * @param productosDistintos productos distintos con existencias (cerradas o abiertas).
 * @param gramosDisponibles suma de gramos que quedan en los rollos abiertos.
 * @param valorInventario   rollos cerrados valorados al precio de referencia del catálogo.
 * @param ingresosTotales   suma de cantidad * precio de todas las ventas registradas.
 * @param gramosConsumidos  suma de gramos gastados históricamente (SALIDA_USO).
 */
public record ResumenDTO(int rollosCerrados, int rollosAbiertos, long productosDistintos,
                         BigDecimal gramosDisponibles, BigDecimal valorInventario,
                         BigDecimal ingresosTotales, BigDecimal gramosConsumidos) {
}

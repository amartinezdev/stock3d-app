package com.alvaromartinez.stock_api.repository;

import com.alvaromartinez.stock_api.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository de Movimiento. Solo usamos el save() que viene gratis, para
 * registrar cada evento del histórico (entrada, venta, consumo).
 */
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
}

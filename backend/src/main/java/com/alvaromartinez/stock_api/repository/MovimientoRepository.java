package com.alvaromartinez.stock_api.repository;

import com.alvaromartinez.stock_api.model.Movimiento;
import com.alvaromartinez.stock_api.model.TipoMovimiento;
import com.alvaromartinez.stock_api.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository de Movimiento: el save() que viene gratis para registrar cada
 * evento del histórico (entrada, venta, consumo), más las dos lecturas que
 * necesita el frontend (histórico paginado y cálculo del resumen).
 */
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /**
     * Histórico de un usuario, paginado. El orden NO va en el nombre del
     * método: lo decide el Pageable (?sort=fecha,desc), así el cliente puede
     * reordenar la tabla sin tocar el backend.
     *
     * @param usuario  dueño de los movimientos.
     * @param pageable página/tamaño/orden pedidos en la URL.
     * @return la página de movimientos con sus metadatos.
     */
    Page<Movimiento> findByUsuario(Usuario usuario, Pageable pageable);

    /**
     * Todos los movimientos de un usuario, sin paginar - solo para calcular
     * los totales del resumen (ingresos, gramos consumidos).
     *
     * @param usuario dueño de los movimientos.
     * @return su histórico completo.
     */
    List<Movimiento> findByUsuario(Usuario usuario);

    /**
     * Igual que el anterior pero filtrando por tipo (solo ventas, solo
     * consumos...). Se hace en la BBDD y no en el frontend porque, con
     * paginación, filtrar en el cliente solo filtraría la página cargada.
     *
     * @param usuario  dueño de los movimientos.
     * @param tipo     tipo por el que filtrar.
     * @param pageable página/tamaño/orden pedidos en la URL.
     * @return la página de movimientos de ese tipo.
     */
    Page<Movimiento> findByUsuarioAndTipo(Usuario usuario, TipoMovimiento tipo, Pageable pageable);
}

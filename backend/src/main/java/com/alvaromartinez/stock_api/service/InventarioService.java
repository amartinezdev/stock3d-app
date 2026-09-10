package com.alvaromartinez.stock_api.service;

import com.alvaromartinez.stock_api.dto.ResumenDTO;
import com.alvaromartinez.stock_api.model.*;
import com.alvaromartinez.stock_api.repository.EnUsoRepository;
import com.alvaromartinez.stock_api.repository.InventarioRepository;
import com.alvaromartinez.stock_api.repository.MovimientoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Lógica de negocio de todas las operaciones de stock: entrada, abrir rollo,
 * consumir gramos y vender. Como ProductoService, no sabe nada de HTTP - eso
 * es cosa de InventarioController. Habla con tres repositories distintos
 * (Inventario, EnUso, Movimiento) porque cada operación puede tocar más de
 * una tabla a la vez (por ejemplo, abrirRollo mueve una unidad de Inventario
 * a EnUso).
 */
@Service
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MovimientoRepository movimientoRepository;
    private final EnUsoRepository enUsoRepository;

    public InventarioService(InventarioRepository inventarioRepository, MovimientoRepository movimientoRepository, EnUsoRepository enUsoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.movimientoRepository = movimientoRepository;
        this.enUsoRepository = enUsoRepository;
    }

    /**
     * Añade "cantidad" rollos cerrados al Inventario de un usuario para un
     * producto (creando la fila si es la primera vez, sumando si ya existía),
     * y registra un Movimiento de tipo ENTRADA para el histórico.
     */
    public Inventario entrada(Usuario usuario, Producto producto, int cantidad) {
        Optional<Inventario> inv = inventarioRepository.findByUsuarioAndProducto(usuario, producto);
        Inventario invRepo;

        if (inv.isPresent()) {
            Inventario inventario = inv.get();
            int cant = inventario.getCantidad() + cantidad;
            inventario.setCantidad(cant);

            invRepo = inventarioRepository.save(inventario);
        } else {
            invRepo = inventarioRepository.save(new Inventario(null, usuario, producto, cantidad));
        }

        // BigDecimal.valueOf(cantidad): Movimiento.cantidad es BigDecimal
        // (también sirve para gramos en otras operaciones), así que el int
        // se convierte antes de guardar.
        movimientoRepository.save(new Movimiento(null, TipoMovimiento.ENTRADA, usuario, producto, BigDecimal.valueOf(cantidad), null, LocalDateTime.now(), null));

        return invRepo;
    }

    /**
     * Abre un rollo: resta 1 al Inventario cerrado del usuario para ese
     * producto, y crea una fila nueva en EnUso con los gramos completos
     * (Producto.pesoRollo). No permite abrir un rollo si no hay ninguno
     * cerrado disponible (ni fila de Inventario, ni cantidad > 0) - la
     * validación de "no stock negativo" para esta operación.
     * A propósito, NO registra ningún Movimiento: abrir un rollo no es una
     * entrada ni una salida real de stock, solo cambia de estado (de
     * "cerrado" a "abierto"), sigue siendo del mismo usuario.
     */
    public EnUso abrirRollo(Usuario usuario, Producto producto) {
        Optional<Inventario> inv = inventarioRepository.findByUsuarioAndProducto(usuario, producto);

        if (inv.isPresent()) {
            Inventario invent = inv.get();
            int cantidad = invent.getCantidad();

            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad es 0 o negativa.");
            }
            invent.setCantidad(cantidad - 1);
            inventarioRepository.save(invent);
        } else {
            throw new IllegalArgumentException("No tienes ningún rollo");
        }

        EnUso rollo = new EnUso(null, usuario, producto, producto.getPesoRollo(), LocalDate.now());
        enUsoRepository.save(rollo);

        return rollo;

    }

    /**
     * Consume "gramos" de un rollo ya abierto (EnUso): valida que no se
     * gaste más de lo que queda (no permitir "stock negativo" también aquí),
     * resta y guarda, y registra un Movimiento SALIDA_USO con los gramos
     * REALMENTE consumidos en esta llamada (el parámetro gramos, no lo que
     * queda tras restar - serían números distintos).
     */
    public EnUso consumirGramos(EnUso enUso, BigDecimal gramos) {
        if (gramos.compareTo(enUso.getGramosRestantes()) > 0) {
            throw new IllegalArgumentException("No puedes gastar más de lo que tienes");
        }

        // BigDecimal es inmutable: subtract(...) devuelve un objeto NUEVO,
        // no modifica ni gramosRestantes ni gramos - por eso "gramos" sigue
        // representando el consumo de esta llamada más abajo, en el Movimiento.
        enUso.setGramosRestantes(enUso.getGramosRestantes().subtract(gramos));

        enUsoRepository.save(enUso);

        Movimiento mov = new Movimiento(null, TipoMovimiento.SALIDA_USO, enUso.getUsuario(), enUso.getProducto(),
                gramos, null, LocalDateTime.now(), null);

        movimientoRepository.save(mov);

        return enUso;
    }


    /**
     * Vende "cant" rollos cerrados del Inventario de un usuario (sin pasar
     * por EnUso - es venta de rollo entero, no de gramos sueltos): valida
     * que no se venda más de lo que hay (no permitir stock negativo), resta
     * y guarda, y registra un Movimiento SALIDA_VENTA con el precio real de
     * ESTA venta (puede no coincidir con Producto.precio, que es solo una
     * referencia/sugerencia).
     */
    public Inventario venta(Usuario usuario, Producto producto, int cant, BigDecimal precio) {
        Optional<Inventario> invOpt = inventarioRepository.findByUsuarioAndProducto(usuario, producto);

        if (invOpt.isPresent()) {
            Inventario inv = invOpt.get();

            if (cant <= inv.getCantidad()) {
                Movimiento mov = new Movimiento(null, TipoMovimiento.SALIDA_VENTA, usuario, producto, BigDecimal.valueOf(cant), precio, LocalDateTime.now(), null);
                movimientoRepository.save(mov);
                inv.setCantidad(inv.getCantidad() - cant);
                inventarioRepository.save(inv);
            } else {
                throw new IllegalArgumentException("No puedes vender más de lo que tienes");
            }
            return inv;
        } else {
            throw new IllegalArgumentException("No se ha encontrado en tu inventario");
        }

    }

    // ------------------------------------------------------------------
    // Lecturas (solo consultan, no modifican nada). Las escribe el mismo
    // Service porque comparten los tres repositories, y así el controller
    // sigue sin hablar nunca directamente con la capa de datos.
    // ------------------------------------------------------------------

    /**
     * Rollos CERRADOS que tiene un usuario, ordenados por nombre de producto.
     *
     * @param usuario dueño del inventario.
     * @return sus filas de Inventario (puede incluir filas con cantidad 0,
     * de productos que llegó a tener y ya no).
     */
    public List<Inventario> listarInventario(Usuario usuario) {
        return inventarioRepository.findByUsuarioOrderByProductoNombreAsc(usuario);
    }

    /**
     * Rollos ABIERTOS de un usuario, del más reciente al más antiguo.
     *
     * @param usuario dueño de los rollos.
     * @return sus filas de EnUso, agotadas incluidas (gramosRestantes = 0).
     */
    public List<EnUso> listarEnUso(Usuario usuario) {
        return enUsoRepository.findByUsuarioOrderByFechaAperturaDescIdDesc(usuario);
    }

    /**
     * Histórico de movimientos de un usuario, paginado.
     *
     * @param usuario  dueño de los movimientos.
     * @param tipo     filtro opcional; null = todos los tipos.
     * @param pageable página/tamaño/orden pedidos en la URL.
     * @return la página de movimientos con sus metadatos.
     */
    public Page<Movimiento> listarMovimientos(Usuario usuario, TipoMovimiento tipo, Pageable pageable) {
        if (tipo == null) {
            return movimientoRepository.findByUsuario(usuario, pageable);
        }
        return movimientoRepository.findByUsuarioAndTipo(usuario, tipo, pageable);
    }

    /**
     * Cifras agregadas del panel principal. Se calculan al vuelo cada vez a
     * partir de las tres tablas: ningún total se guarda en BBDD, así no hay
     * forma de que el resumen se desincronice de los datos reales.
     * Los rollos abiertos con 0 gramos no cuentan como "abiertos" ni suman
     * gramos: están agotados aunque su fila siga existiendo como histórico.
     *
     * @param usuario usuario del que se resume el stock.
     * @return el resumen listo para pintar en el frontend.
     */
    public ResumenDTO resumen(Usuario usuario) {
        List<Inventario> inventario = listarInventario(usuario);
        List<Movimiento> movimientos = movimientoRepository.findByUsuario(usuario);

        List<EnUso> abiertos = listarEnUso(usuario).stream()
                .filter(uso -> uso.getGramosRestantes().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        int rollosCerrados = inventario.stream()
                .mapToInt(Inventario::getCantidad)
                .sum();

        // reduce(ZERO, add) en vez de sum(): BigDecimal no es un tipo
        // primitivo, no existe mapToBigDecimal().
        BigDecimal valorInventario = inventario.stream()
                .map(inv -> inv.getProducto().getPrecio().multiply(BigDecimal.valueOf(inv.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gramosDisponibles = abiertos.stream()
                .map(EnUso::getGramosRestantes)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Se comparan ids de producto, no objetos Producto: dos entidades
        // cargadas en consultas distintas pueden ser instancias diferentes
        // de la misma fila, y distinct() usa equals().
        long productosDistintos = Stream.concat(
                        inventario.stream().filter(inv -> inv.getCantidad() > 0).map(inv -> inv.getProducto().getId()),
                        abiertos.stream().map(uso -> uso.getProducto().getId()))
                .distinct()
                .count();

        BigDecimal ingresosTotales = movimientos.stream()
                .filter(mov -> mov.getTipo() == TipoMovimiento.SALIDA_VENTA && mov.getPrecio() != null)
                .map(mov -> mov.getPrecio().multiply(mov.getCantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gramosConsumidos = movimientos.stream()
                .filter(mov -> mov.getTipo() == TipoMovimiento.SALIDA_USO)
                .map(Movimiento::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ResumenDTO(rollosCerrados, abiertos.size(), productosDistintos,
                gramosDisponibles, valorInventario, ingresosTotales, gramosConsumidos);
    }
}

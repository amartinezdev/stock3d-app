package com.alvaromartinez.stock_api.service;

import com.alvaromartinez.stock_api.model.*;
import com.alvaromartinez.stock_api.repository.EnUsoRepository;
import com.alvaromartinez.stock_api.repository.InventarioRepository;
import com.alvaromartinez.stock_api.repository.MovimientoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private EnUsoRepository enUsoRepository;

    @InjectMocks
    private InventarioService inventarioService;

    @Test
    void abrirRollo_sinInventario_lanzaExcepcion() {
        Usuario usuario = new Usuario();
        Producto producto = new Producto();

        when(inventarioRepository.findByUsuarioAndProducto(usuario, producto))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> inventarioService.abrirRollo(usuario, producto));
    }

    @Test
    void abrirRollo_conInventario() {
        Usuario usuario = new Usuario();
        Producto producto = new Producto();
        BigDecimal pesoRollo = producto.getPesoRollo();

        producto.setPesoRollo(pesoRollo);

        Inventario inventario = new Inventario(null, usuario, producto, 5);

        when(inventarioRepository.findByUsuarioAndProducto(usuario, producto))
                .thenReturn(Optional.of(inventario));

        EnUso resultado = inventarioService.abrirRollo(usuario, producto);

        assertEquals(pesoRollo, resultado.getGramosRestantes());

        assertEquals(4, inventario.getCantidad());

        verify(inventarioRepository).save(inventario);
    }

    @Test
    void entrada_sinInventario_crearInventario() {
        Usuario usuario = new Usuario();
        Producto producto = new Producto();
        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);

        when(inventarioRepository.findByUsuarioAndProducto(usuario, producto))
                .thenReturn(Optional.empty());

        inventarioService.entrada(usuario, producto, 5);

        verify(inventarioRepository).save(captor.capture());

        Inventario capturado = captor.getValue();

        assertEquals(5, capturado.getCantidad());
    }

    @Test
    void entrada_conInventario() {
        Usuario usuario = new Usuario();
        Producto producto = new Producto();
        Inventario inventario = new Inventario(null, usuario, producto, 5);

        when(inventarioRepository.findByUsuarioAndProducto(usuario, producto))
                .thenReturn(Optional.of(inventario));

        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        Inventario resultado = inventarioService.entrada(usuario, producto, 1);

        verify(inventarioRepository).save(resultado);

        assertEquals(6, resultado.getCantidad());
    }

    @Test
    void venta_sinInventario() {
        Usuario usuario = new Usuario();
        Producto producto = new Producto();


        when(inventarioRepository.findByUsuarioAndProducto(usuario, producto))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> inventarioService.venta(usuario, producto, 5, BigDecimal.valueOf(10)));
    }

    @Test
    void venta_fueraDeLimites() {
        Usuario usuario = new Usuario();
        Producto producto = new Producto();
        Inventario inventario = new Inventario(null, usuario, producto, 5);

        when(inventarioRepository.findByUsuarioAndProducto(usuario, producto))
                .thenReturn(Optional.of(inventario));

        assertThrows(IllegalArgumentException.class,
                () -> inventarioService.venta(usuario, producto, 10, BigDecimal.valueOf(10)));
    }

    @Test
    void venta_ok() {
        Usuario usuario = new Usuario();
        Producto producto = new Producto();
        Inventario inventario = new Inventario(null, usuario, producto, 5);
        ArgumentCaptor<Movimiento> captor = ArgumentCaptor.forClass(Movimiento.class);

        when(inventarioRepository.findByUsuarioAndProducto(usuario, producto))
                .thenReturn(Optional.of(inventario));


        inventario = inventarioService.venta(usuario, producto, 3, BigDecimal.valueOf(10));

        // movimiento
        verify(movimientoRepository).save(captor.capture());
        Movimiento capturado = captor.getValue();
        assertEquals(BigDecimal.valueOf(3), capturado.getCantidad());

        // restar al stock
        assertEquals(2, inventario.getCantidad());

    }

    @Test
    void consumirGramos_fueraDeLimites() {
        Usuario usuario = new Usuario();
        Producto producto = new Producto();
        EnUso uso = new EnUso(null, usuario, producto, BigDecimal.valueOf(1000), LocalDate.now());

        assertThrows(IllegalArgumentException.class, () -> inventarioService.consumirGramos(uso, BigDecimal.valueOf(1100)));
    }

    @Test
    void consumirGramos_ok() {
        Usuario usuario = new Usuario();
        Producto producto = new Producto();
        EnUso uso = new EnUso(null, usuario, producto, BigDecimal.valueOf(1000), LocalDate.now());
        ArgumentCaptor<Movimiento> captor = ArgumentCaptor.forClass(Movimiento.class);

        inventarioService.consumirGramos(uso, BigDecimal.valueOf(500));

        verify(movimientoRepository).save(captor.capture());
        Movimiento mov = captor.getValue();
        assertEquals(new BigDecimal(500), mov.getCantidad());

        assertEquals(new BigDecimal(500), uso.getGramosRestantes());
    }


}

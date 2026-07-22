package com.alvaromartinez.stock_api.service;

import com.alvaromartinez.stock_api.model.Producto;
import com.alvaromartinez.stock_api.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void obtenerPorId_existente() {
        Producto producto = new Producto();
        producto.setId(20L);
        when(productoRepository.findById(producto.getId())).thenReturn(Optional.of(producto));

        Producto resultado = productoService.obtenerPorId(producto.getId());

        assertEquals(producto.getId(), resultado.getId());
    }

    @Test
    void obtenerPorId_inexistente() {
        Producto producto = new Producto();
        producto.setId(20L);
        when(productoRepository.findById(producto.getId())).thenReturn(Optional.empty());

        Producto resultado = productoService.obtenerPorId(producto.getId());

        assertNull(resultado);
    }

    @Test
    void crear() {
        Producto producto = new Producto();
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.crear(producto);
        verify(productoRepository).save(resultado);
    }

    @Test
    void actualizar() {
        Producto producto = new Producto();
        Long idUrl = 3L;
        producto.setId(99L);
        when(productoRepository.existsById(idUrl)).thenReturn(true);

        Producto resultado = productoService.actualizar(idUrl, producto);
        verify(productoRepository).save(resultado);

        assertEquals(3, producto.getId());
    }

    @Test
    void actualizar_noExistente() {
        Producto producto = new Producto();
        producto.setId(9L);
        when(productoRepository.existsById(1L)).thenReturn(false);

        Producto resultado = productoService.actualizar(1L, producto);

        assertNull(resultado);
    }

    @Test
    void eliminar_existente() {
        Producto producto = new Producto();
        producto.setId(9L);

        when(productoRepository.existsById(producto.getId())).thenReturn(true);

        boolean resultado = productoService.eliminar(producto.getId());

        verify(productoRepository).deleteById(producto.getId());

        assertTrue(resultado);
    }

    @Test
    void eliminar_noExistente() {
        Producto producto = new Producto();
        producto.setId(9L);

        when(productoRepository.existsById(producto.getId())).thenReturn(false);

        boolean resultado = productoService.eliminar(producto.getId());

        verify(productoRepository, never()).deleteById(producto.getId());

        assertFalse(resultado);
    }
}


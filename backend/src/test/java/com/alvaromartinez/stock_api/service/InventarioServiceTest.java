package com.alvaromartinez.stock_api.service;

import com.alvaromartinez.stock_api.model.EnUso;
import com.alvaromartinez.stock_api.model.Inventario;
import com.alvaromartinez.stock_api.model.Producto;
import com.alvaromartinez.stock_api.model.Usuario;
import com.alvaromartinez.stock_api.repository.EnUsoRepository;
import com.alvaromartinez.stock_api.repository.InventarioRepository;
import com.alvaromartinez.stock_api.repository.MovimientoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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


}

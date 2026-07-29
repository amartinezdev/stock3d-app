package com.alvaromartinez.stock_api.controller;


import com.alvaromartinez.stock_api.model.*;
import com.alvaromartinez.stock_api.repository.EnUsoRepository;
import com.alvaromartinez.stock_api.repository.InventarioRepository;
import com.alvaromartinez.stock_api.repository.ProductoRepository;
import com.alvaromartinez.stock_api.repository.UsuarioRepository;
import com.alvaromartinez.stock_api.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InventarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private EnUsoRepository enUsoRepository;

    @Test
    void entrada_admin() throws Exception {
        Usuario user = usuarioRepository.save(new Usuario());
        user.setUserName("123");

        Producto pro = productoRepository.save(new Producto(null, "Patatas", "descripcion", BigDecimal.valueOf(10), Categoria.PLA, BigDecimal.valueOf(1)));

        String token = jwtUtil.generarToken(user.getUserName(), "ADMIN");

        String json = String.format("""                                                                      
                {
                  "productoId": %d,
                  "cantidad": 3
                }
                """, pro.getId());

        mockMvc.perform(post("/inventario/entrada")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

    }

    @Test
    void entrada_user() throws Exception {
        Usuario user = usuarioRepository.save(new Usuario());
        user.setUserName("123");

        Producto pro = productoRepository.save(new Producto(null, "Patatas", "descripcion", BigDecimal.valueOf(10), Categoria.PLA, BigDecimal.valueOf(1)));

        String token = jwtUtil.generarToken(user.getUserName(), "USER");

        String json = String.format("""                                                                      
                {
                  "productoId": %d,
                  "cantidad": 3
                }
                """, pro.getId());

        mockMvc.perform(post("/inventario/entrada")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

    }

    @Test
    void entrada_noLogin() throws Exception {
        Usuario user = usuarioRepository.save(new Usuario());
        user.setUserName("123");

        Producto pro = productoRepository.save(new Producto(null, "Patatas", "descripcion", BigDecimal.valueOf(10), Categoria.PLA, BigDecimal.valueOf(1)));


        String json = String.format("""                                                                      
                {
                  "productoId": %d,
                  "cantidad": 3
                }
                """, pro.getId());

        mockMvc.perform(post("/inventario/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());

    }

    @Test
    void abrir_rollo_user() throws Exception {
        Usuario user = usuarioRepository.save(new Usuario());
        user.setUserName("123");

        Producto pro = productoRepository.save(new Producto(null, "Patatas", "descripcion", BigDecimal.valueOf(10), Categoria.PLA, BigDecimal.valueOf(1)));

        String token = jwtUtil.generarToken(user.getUserName(), "USER");

        String json = String.format("""                                                                      
                {
                  "productoId": %d
                }
                """, pro.getId());

        inventarioRepository.save(new Inventario(null, user, pro, 1));


        mockMvc.perform(post("/inventario/abrirRollo")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

    }

    @Test
    void consumir_gramos_user() throws Exception {
        Usuario user = usuarioRepository.save(new Usuario());
        user.setUserName("123");

        Producto pro = productoRepository.save(new Producto(null, "Patatas", "descripcion", BigDecimal.valueOf(10), Categoria.PLA, BigDecimal.valueOf(1)));

        String token = jwtUtil.generarToken(user.getUserName(), "USER");

        inventarioRepository.save(new Inventario(null, user, pro, 1));
        EnUso uso = enUsoRepository.save(new EnUso(null, user, pro, BigDecimal.valueOf(1000), LocalDate.now()));

        String json = String.format("""                                                                      
                {
                  "id": %d,
                  "gramos": 300
                }
                """, uso.getId());


        mockMvc.perform(post("/inventario/consumir")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void venta() throws Exception {
        Usuario user = usuarioRepository.save(new Usuario());
        user.setUserName("123");

        Producto pro = productoRepository.save(new Producto(null, "Patatas", "descripcion", BigDecimal.valueOf(10), Categoria.PLA, BigDecimal.valueOf(1)));

        String token = jwtUtil.generarToken(user.getUserName(), "USER");

        String json = String.format("""                                                                      
                {
                  "productoId": %d,
                  "cantidad": 1,
                  "precio": 100
                }
                """, pro.getId());

        inventarioRepository.save(new Inventario(null, user, pro, 1));
        enUsoRepository.save(new EnUso(null, user, pro, BigDecimal.valueOf(1000), LocalDate.now()));


        mockMvc.perform(post("/inventario/vender")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
}

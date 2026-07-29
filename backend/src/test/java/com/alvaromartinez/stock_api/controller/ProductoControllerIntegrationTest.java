package com.alvaromartinez.stock_api.controller;

import com.alvaromartinez.stock_api.model.Categoria;
import com.alvaromartinez.stock_api.model.Producto;
import com.alvaromartinez.stock_api.repository.ProductoRepository;
import com.alvaromartinez.stock_api.security.JwtUtil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProductoRepository productoRepository;

    @Test
    void listar_sinToken_devuelveForbidden() throws Exception {
        mockMvc.perform(get("/productos")).andExpect(status().isForbidden());
    }

    @Test
    void listar_conToken() throws Exception {
        String token = jwtUtil.generarToken("123", "USER");

        mockMvc.perform(get("/productos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void obtener_porId_sinLogin() throws Exception {
        mockMvc.perform(get("/productos/{id}", 1L)).andExpect(status().isForbidden());
    }

    @Test
    void obtener_porId() throws Exception {
        String token = jwtUtil.generarToken("123", "USER");

        mockMvc.perform(get("/productos/{id}", 1L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void crear_admin() throws Exception {
        String token = jwtUtil.generarToken("123", "ADMIN");

        String json = """
                {
                  "nombre": "PLA Blanco",
                  "descripcion": "Filamento PLA blanco 1kg",
                  "precio": 19.99,
                  "categoria": "PLA",
                  "pesoRollo": 1.0
                }
                """;

        mockMvc.perform(post("/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void crear_user() throws Exception {
        String token = jwtUtil.generarToken("123", "USER");

        String json = """
                {
                  "nombre": "PLA Blanco",
                  "descripcion": "Filamento PLA blanco 1kg",
                  "precio": 19.99,
                  "categoria": "PLA",
                  "pesoRollo": 1.0
                }
                """;

        mockMvc.perform(post("/productos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }


    @Test
    void delete_admin() throws Exception {
        String token = jwtUtil.generarToken("123", "ADMIN");

        Producto pro = new Producto(null, "prueba", "descripcion", BigDecimal.valueOf(10), Categoria.PLA, BigDecimal.valueOf(1.2));

        productoRepository.save(pro);

        mockMvc.perform(delete("/productos/{id}", pro.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_user() throws Exception {
        String token = jwtUtil.generarToken("123", "USER");

        Producto pro = new Producto(null, "prueba", "descripcion", BigDecimal.valueOf(10), Categoria.PLA, BigDecimal.valueOf(1.2));

        productoRepository.save(pro);

        mockMvc.perform(delete("/productos/{id}", pro.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_noLogin() throws Exception {

        Producto pro = new Producto(null, "prueba", "descripcion", BigDecimal.valueOf(10), Categoria.PLA, BigDecimal.valueOf(1.2));

        productoRepository.save(pro);

        mockMvc.perform(delete("/productos/{id}", pro.getId()))
                .andExpect(status().isForbidden());
    }
}

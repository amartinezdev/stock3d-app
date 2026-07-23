package com.alvaromartinez.stock_api.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UsuarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registrarEndpoint_devuelveOk() throws Exception {
        String json = """
                 {
                     "nombre": "Alvaro",
                     "email": "alvaro@gmail.com",
                     "userName": "alvaro",
                     "password": "123"
                 }
                """;

        mockMvc.perform(post("/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("alvaro"));

    }

    @Test
    void registrarEndpoint_duplicado_devuelve400() throws Exception {
        String json = """
                 {
                     "nombre": "Alvaro",
                     "email": "alvaro@gmail.com",
                     "userName": "alvaro",
                     "password": "123"
                 }
                """;

        mockMvc.perform(post("/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }


}

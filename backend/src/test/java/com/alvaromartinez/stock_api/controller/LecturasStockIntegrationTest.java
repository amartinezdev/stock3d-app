package com.alvaromartinez.stock_api.controller;

import com.alvaromartinez.stock_api.model.*;
import com.alvaromartinez.stock_api.repository.EnUsoRepository;
import com.alvaromartinez.stock_api.repository.InventarioRepository;
import com.alvaromartinez.stock_api.repository.MovimientoRepository;
import com.alvaromartinez.stock_api.repository.ProductoRepository;
import com.alvaromartinez.stock_api.repository.UsuarioRepository;
import com.alvaromartinez.stock_api.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de los endpoints de LECTURA del stock (GET /inventario,
 * /inventario/enUso, /inventario/resumen, /movimientos y /me), que son los
 * que consume el frontend.
 *
 * Lo que más se comprueba aquí no es el "feliz camino" sino el aislamiento
 * entre usuarios: todos estos endpoints resuelven el usuario a partir del
 * JWT, así que hay que asegurarse de que a nadie se le cuela el stock de
 * otra persona en su listado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LecturasStockIntegrationTest {

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

    @Autowired
    private MovimientoRepository movimientoRepository;

    /* ---------- GET /inventario ---------- */

    @Test
    void inventario_sinToken_devuelveForbidden() throws Exception {
        mockMvc.perform(get("/inventario")).andExpect(status().isForbidden());
    }

    @Test
    void inventario_devuelveSoloLoDelUsuarioAutenticado() throws Exception {
        Usuario ana = crearUsuario("ana");
        Usuario bruno = crearUsuario("bruno");

        Producto pla = crearProducto("PLA Negro", BigDecimal.valueOf(20));
        Producto petg = crearProducto("PETG Azul", BigDecimal.valueOf(30));

        inventarioRepository.save(new Inventario(null, ana, pla, 4));
        inventarioRepository.save(new Inventario(null, bruno, petg, 9));

        mockMvc.perform(get("/inventario").header("Authorization", token(ana)))
                .andExpect(status().isOk())
                // Solo una fila: la de Ana. La de Bruno no puede aparecer
                // aunque esté en la misma tabla.
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].producto.nombre").value("PLA Negro"))
                .andExpect(jsonPath("$[0].cantidad").value(4));
    }

    /* ---------- GET /inventario/enUso ---------- */

    @Test
    void enUso_devuelveLosRollosAbiertosDelUsuario() throws Exception {
        Usuario ana = crearUsuario("ana");
        Producto pla = crearProducto("PLA Negro", BigDecimal.valueOf(20));

        enUsoRepository.save(new EnUso(null, ana, pla, BigDecimal.valueOf(650), LocalDate.now()));

        mockMvc.perform(get("/inventario/enUso").header("Authorization", token(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].gramosRestantes").value(650))
                .andExpect(jsonPath("$[0].producto.nombre").value("PLA Negro"));
    }

    /* ---------- GET /inventario/resumen ---------- */

    @Test
    void resumen_sumaRollosGramosYValor() throws Exception {
        Usuario ana = crearUsuario("ana");
        Producto pla = crearProducto("PLA Negro", BigDecimal.valueOf(20));

        inventarioRepository.save(new Inventario(null, ana, pla, 3));
        enUsoRepository.save(new EnUso(null, ana, pla, BigDecimal.valueOf(500), LocalDate.now()));
        // Rollo ya agotado: no debe contar ni como abierto ni en gramos.
        enUsoRepository.save(new EnUso(null, ana, pla, BigDecimal.ZERO, LocalDate.now()));

        mockMvc.perform(get("/inventario/resumen").header("Authorization", token(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollosCerrados").value(3))
                .andExpect(jsonPath("$.rollosAbiertos").value(1))
                .andExpect(jsonPath("$.gramosDisponibles").value(500))
                .andExpect(jsonPath("$.productosDistintos").value(1))
                // 3 rollos * 20 € de precio de referencia.
                .andExpect(jsonPath("$.valorInventario").value(60));
    }

    @Test
    void resumen_sumaIngresosDeLasVentas() throws Exception {
        Usuario ana = crearUsuario("ana");
        Producto pla = crearProducto("PLA Negro", BigDecimal.valueOf(20));

        // 2 rollos vendidos a 25 € = 50 € de ingresos.
        movimientoRepository.save(new Movimiento(null, TipoMovimiento.SALIDA_VENTA, ana, pla,
                BigDecimal.valueOf(2), BigDecimal.valueOf(25), LocalDateTime.now(), null));
        movimientoRepository.save(new Movimiento(null, TipoMovimiento.SALIDA_USO, ana, pla,
                BigDecimal.valueOf(120), null, LocalDateTime.now(), null));

        mockMvc.perform(get("/inventario/resumen").header("Authorization", token(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingresosTotales").value(50))
                .andExpect(jsonPath("$.gramosConsumidos").value(120));
    }

    /* ---------- GET /movimientos ---------- */

    @Test
    void movimientos_devuelvePaginaDelUsuario() throws Exception {
        Usuario ana = crearUsuario("ana");
        Usuario bruno = crearUsuario("bruno");
        Producto pla = crearProducto("PLA Negro", BigDecimal.valueOf(20));

        movimientoRepository.save(new Movimiento(null, TipoMovimiento.ENTRADA, ana, pla,
                BigDecimal.valueOf(5), null, LocalDateTime.now(), null));
        movimientoRepository.save(new Movimiento(null, TipoMovimiento.ENTRADA, bruno, pla,
                BigDecimal.valueOf(7), null, LocalDateTime.now(), null));

        mockMvc.perform(get("/movimientos").header("Authorization", token(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].cantidad").value(5));
    }

    @Test
    void movimientos_filtraPorTipo() throws Exception {
        Usuario ana = crearUsuario("ana");
        Producto pla = crearProducto("PLA Negro", BigDecimal.valueOf(20));

        movimientoRepository.save(new Movimiento(null, TipoMovimiento.ENTRADA, ana, pla,
                BigDecimal.valueOf(5), null, LocalDateTime.now(), null));
        movimientoRepository.save(new Movimiento(null, TipoMovimiento.SALIDA_VENTA, ana, pla,
                BigDecimal.ONE, BigDecimal.valueOf(22), LocalDateTime.now(), null));

        mockMvc.perform(get("/movimientos")
                        .param("tipo", "SALIDA_VENTA")
                        .header("Authorization", token(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].tipo").value("SALIDA_VENTA"))
                .andExpect(jsonPath("$.content[0].precio").value(22));
    }

    /* ---------- GET /me ---------- */

    @Test
    void me_devuelveLosDatosDelUsuarioDelToken() throws Exception {
        Usuario ana = crearUsuario("ana");

        mockMvc.perform(get("/me").header("Authorization", token(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("ana"))
                .andExpect(jsonPath("$.rol").value("USER"))
                // El hash de la contraseña NUNCA debe salir en la respuesta.
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void me_sinToken_devuelveForbidden() throws Exception {
        mockMvc.perform(get("/me")).andExpect(status().isForbidden());
    }

    /* ---------- Ayudantes ---------- */

    private Usuario crearUsuario(String userName) {
        Usuario usuario = new Usuario();
        usuario.setNombre(userName);
        usuario.setEmail(userName + "@correo.com");
        usuario.setUserName(userName);
        usuario.setPassword("hash-falso");
        usuario.setRol(Rol.USER);

        return usuarioRepository.save(usuario);
    }

    private Producto crearProducto(String nombre, BigDecimal precio) {
        return productoRepository.save(
                new Producto(null, nombre, "descripcion", precio, Categoria.PLA, BigDecimal.valueOf(1000)));
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtUtil.generarToken(usuario.getUserName(), usuario.getRol().name());
    }
}

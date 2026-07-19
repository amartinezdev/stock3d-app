package com.alvaromartinez.stock_api.controller;

import com.alvaromartinez.stock_api.dto.*;
import com.alvaromartinez.stock_api.model.EnUso;
import com.alvaromartinez.stock_api.model.Inventario;
import com.alvaromartinez.stock_api.model.Producto;
import com.alvaromartinez.stock_api.model.Usuario;
import com.alvaromartinez.stock_api.repository.EnUsoRepository;
import com.alvaromartinez.stock_api.repository.ProductoRepository;
import com.alvaromartinez.stock_api.repository.UsuarioRepository;
import com.alvaromartinez.stock_api.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

/**
 * Endpoints de todas las operaciones de stock (entrada, abrir rollo,
 * consumir gramos, vender). En NINGUNO de los métodos se acepta un Usuario
 * como dato de entrada del cliente: el usuario que hace la petición siempre
 * se resuelve a partir de Authentication (el JWT ya verificado por
 * JwtAuthFilter), nunca del JSON - si se aceptara un Usuario del cliente,
 * cualquiera podría mandar el id de otra persona y actuar en su nombre.
 * Las respuestas usan DTOs propios (InventarioResponseDTO, etc.) en vez de
 * devolver las entidades directamente, para no filtrar nunca la contraseña
 * (hasheada) de Usuario anidada dentro de la respuesta - usan
 * UsuarioResponseDTO en su lugar.
 */
@RestController
public class InventarioController {
    private final InventarioService inventarioService;

    private final UsuarioRepository usuarioRepository;

    private final ProductoRepository productoRepository;

    private final EnUsoRepository enUsoRepository;


    public InventarioController(InventarioService inventarioService, UsuarioRepository usuarioRepository, ProductoRepository productoRepository, EnUsoRepository enUsoRepository) {
        this.inventarioService = inventarioService;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.enUsoRepository = enUsoRepository;
    }

    /**
     * Añade stock cerrado (rollos) al inventario personal del usuario
     * autenticado. 404 si el productoId del DTO no existe en el catálogo.
     */
    @PostMapping("/inventario/entrada")
    public ResponseEntity<InventarioResponseDTO> entrada(Authentication auth, @Valid @RequestBody EntradaStockDTO dto) {
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUserName(username).orElseThrow();
        UsuarioResponseDTO usuarioDTO = new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getUserName(),
                usuario.getRol()
        );
        Optional<Producto> productoOpt = productoRepository.findById(dto.productoId());

        if (productoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Producto producto = productoOpt.get();

        Inventario inv = inventarioService.entrada(usuario, producto, dto.cantidad());

        // La respuesta se construye con el Inventario REAL devuelto por el
        // Service (ya guardado en BBDD), no con los datos de entrada -
        // así se refleja siempre el estado real tras la operación.
        InventarioResponseDTO responseDTO = new InventarioResponseDTO(
                inv.getId(),
                usuarioDTO,
                inv.getProducto(),
                inv.getCantidad()
        );

        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Abre un rollo cerrado del inventario del usuario autenticado (-1 en
     * Inventario, +1 fila nueva en EnUso con el peso completo del rollo).
     * 404 si el productoId no existe en el catálogo.
     */
    @PostMapping("/inventario/abrirRollo")
    public ResponseEntity<AbrirRolloResponseDTO> abrirRollo(Authentication auth, @Valid @RequestBody AbrirRolloDTO dto) {
        String userName = auth.getName();
        Usuario usuario = usuarioRepository.findByUserName(userName).orElseThrow();

        UsuarioResponseDTO user = new UsuarioResponseDTO(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getUserName(), usuario.getRol());

        Optional<Producto> proOpt = productoRepository.findById(dto.productoId());

        if (proOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Producto pro = proOpt.get();
        EnUso abrirRollo = inventarioService.abrirRollo(usuario, pro);

        // Igual que en entrada: la respuesta refleja el EnUso REAL ya
        // guardado (id, gramosRestantes y fechaApertura reales), no valores
        // recalculados o inventados en el controller.
        AbrirRolloResponseDTO rolloResponse = new AbrirRolloResponseDTO(abrirRollo.getId(), user, pro, abrirRollo.getGramosRestantes(), abrirRollo.getFechaApertura());

        return ResponseEntity.ok(rolloResponse);
    }

    /**
     * Consume gramos de un rollo YA abierto (EnUso), identificado por su
     * propio id (el cliente elige de cuál de sus rollos abiertos consume).
     * Antes de tocar nada, comprueba que ese EnUso pertenezca de verdad al
     * usuario autenticado (comparando por id, no por ==/equals de objeto) -
     * sin esta comprobación, cualquier usuario autenticado podría adivinar
     * o probar ids de EnUso ajenos y consumir del rollo de otra persona
     * (IDOR: Insecure Direct Object Reference).
     */
    @PostMapping("/inventario/consumir")
    public ResponseEntity<EnUsoResponseDTO> consumir(Authentication auth, @Valid @RequestBody EnUsoDTO dto) {
        Optional<EnUso> usoOpt = enUsoRepository.findById(dto.id());
        String userNameAuth = auth.getName();
        Usuario user = usuarioRepository.findByUserName(userNameAuth).orElseThrow();

        if (usoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EnUso uso = usoOpt.get();

        if (Objects.equals(uso.getUsuario().getId(), user.getId())) {
            // consumirGramos muta el MISMO objeto "uso" (misma referencia),
            // así que uso.getGramosRestantes() de abajo ya refleja el valor
            // actualizado tras el consumo, sin necesidad de reasignar "uso".
            inventarioService.consumirGramos(uso, dto.gramos());

            UsuarioResponseDTO usuarioFinal = new UsuarioResponseDTO(user.getId(), user.getNombre(), user.getEmail(), user.getUserName(), user.getRol());

            return ResponseEntity.ok(new EnUsoResponseDTO(usuarioFinal, uso.getProducto(), uso.getGramosRestantes()));

        } else {
            return ResponseEntity.notFound().build();
        }

    }

    /**
     * Vende "cantidad" rollos cerrados (sin abrir) del inventario del
     * usuario autenticado, al precio indicado en el DTO (puede diferir del
     * precio de referencia del catálogo). 404 si el producto o el usuario
     * no se encuentran (el usuario debería existir siempre si el JWT es
     * válido, pero se comprueba igual por consistencia con el Optional).
     */
    @PostMapping("/inventario/vender")
    public ResponseEntity<VentaResponseDTO> venta(Authentication auth, @Valid @RequestBody VentaDTO dto) {
        Optional<Producto> proOpt = productoRepository.findById(dto.productoId());
        Optional<Usuario> userOpt = usuarioRepository.findByUserName(auth.getName());

        if (proOpt.isEmpty() || userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Producto pro = proOpt.get();
        Usuario user = userOpt.get();

        Inventario inv = inventarioService.venta(user, pro, dto.cantidad(), dto.precio());

        UsuarioResponseDTO respuesta = new UsuarioResponseDTO(user.getId(), user.getNombre(), user.getEmail(), user.getUserName(), user.getRol());

        // inv.getCantidad() es el Inventario REAL tras la venta (ya
        // descontado); dto.cantidad()/dto.precio() se devuelven tal cual
        // porque son justo los datos de ESTA venta concreta, no algo que
        // el Service recalcule o guarde en otro sitio.
        VentaResponseDTO venta = new VentaResponseDTO(respuesta, inv.getProducto(), inv.getCantidad(), dto.precio());

        return ResponseEntity.ok(venta);


    }
}

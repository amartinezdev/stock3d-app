package com.alvaromartinez.stock_api.controller;

import com.alvaromartinez.stock_api.dto.MovimientoResponseDTO;
import com.alvaromartinez.stock_api.model.TipoMovimiento;
import com.alvaromartinez.stock_api.model.Usuario;
import com.alvaromartinez.stock_api.repository.UsuarioRepository;
import com.alvaromartinez.stock_api.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de solo lectura del histórico de stock. Va en su propio
 * controller (y no en InventarioController) porque "movimiento" es un
 * recurso distinto: Inventario/EnUso son el estado ACTUAL y se modifican;
 * Movimiento es el registro de lo que ya pasó y nunca se edita.
 * El histórico que se devuelve es siempre el del usuario del JWT.
 */
@RestController
public class MovimientoController {

    private final InventarioService inventarioService;

    private final UsuarioRepository usuarioRepository;

    public MovimientoController(InventarioService inventarioService, UsuarioRepository usuarioRepository) {
        this.inventarioService = inventarioService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * GET /movimientos - histórico paginado del usuario autenticado.
     *
     * @param tipo     filtro opcional por tipo (?tipo=SALIDA_VENTA); si no
     *                 viene, se devuelven todos.
     * @param pageable página/tamaño/orden (?page=&size=&sort=fecha,desc) -
     *                 Spring lo rellena solo al ver el tipo en la firma.
     * @return 200 con la página de movimientos y sus metadatos.
     */
    @Operation(summary = "Lista el histórico de movimientos del usuario autenticado", description = "Paginado y ordenable desde la URL, por ejemplo ?sort=fecha,desc.")
    @ApiResponse(responseCode = "200", description = "Página del histórico devuelta correctamente.")
    @GetMapping("/movimientos")
    public ResponseEntity<Page<MovimientoResponseDTO>> listar(Authentication auth,
                                                             @Parameter(description = "Filtro opcional por tipo de movimiento.") @RequestParam(required = false) TipoMovimiento tipo,
                                                             Pageable pageable) {
        Usuario usuario = usuarioRepository.findByUserName(auth.getName()).orElseThrow();

        // Page.map conserva los metadatos de la página (totalElements,
        // totalPages...) y solo transforma el contenido entidad -> DTO.
        Page<MovimientoResponseDTO> pagina = inventarioService.listarMovimientos(usuario, tipo, pageable)
                .map(mov -> new MovimientoResponseDTO(mov.getId(), mov.getTipo(), mov.getProducto(),
                        mov.getCantidad(), mov.getPrecio(), mov.getFecha(), mov.getNotas()));

        return ResponseEntity.ok(pagina);
    }
}

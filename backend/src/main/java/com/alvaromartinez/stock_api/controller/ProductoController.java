package com.alvaromartinez.stock_api.controller;

import com.alvaromartinez.stock_api.dto.ProductoDTO;
import com.alvaromartinez.stock_api.model.Categoria;
import com.alvaromartinez.stock_api.model.Producto;
import com.alvaromartinez.stock_api.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints CRUD del catálogo de productos. @RestController = recibe las
 * peticiones HTTP y devuelve JSON directamente. No tiene lógica de negocio:
 * traduce HTTP a Java y delega en ProductoService.
 */
@RestController
public class ProductoController {

    private final ProductoService productoService;

    // Spring inyecta aquí el bean de ProductoService ya creado por él;
    // este constructor no lo crea, solo lo recibe y lo guarda en el campo.
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * GET /productos - listado paginado, con filtro opcional por categoría.
     *
     * @param categoria filtro opcional (@RequestParam(required = false):
     *                  llega como null si no viene en la URL).
     * @param pageable  página/tamaño/orden (?page=&size=&sort=) - Spring lo
     *                  rellena solo al ver el tipo Pageable en la firma.
     * @return 200 con la página pedida: su contenido más los metadatos
     * (totalElements, totalPages...).
     */
    @Operation(summary = "Lista productos del catálogo", description = "Devuelve el catálogo paginado, opcionalmente filtrado por categoría.")
    @ApiResponse(responseCode = "200", description = "Página de productos devuelta correctamente.")
    @GetMapping("/productos")
    public ResponseEntity<Page<Producto>> listar(@RequestParam(required = false) Categoria categoria, Pageable pageable) {
        return ResponseEntity.ok(productoService.listar(categoria, pageable));
    }

    /**
     * GET /productos/{id} - detalle de un producto.
     *
     * @param id id del producto (@PathVariable: sale del trozo {id} de la URL).
     * @return 200 con el producto de ese id.
     */
    @Operation(summary = "Obtiene el detalle de un producto por su id")
    @ApiResponse(responseCode = "200", description = "Producto encontrado.")
    @ApiResponse(responseCode = "404", description = "No existe ningún producto con ese id.")
    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        Producto producto = productoService.obtenerPorId(id);

        // El Service devuelve null cuando no existe: sin esta comprobación
        // la respuesta era un 200 con el cuerpo vacío, que para un cliente
        // significa "existe y no tiene datos" en vez de "no existe".
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(producto);
    }

    /**
     * POST /productos - crea un producto en el catálogo (solo ADMIN, ver
     * SecurityConfig). Con @Valid, si el DTO no pasa sus validaciones ni se
     * entra al método: salta directo al GlobalExceptionHandler.
     *
     * @param productoDTO datos del producto, convertidos del JSON por @RequestBody.
     * @return 201 (creado) con el producto ya guardado, con su id generado.
     */
    @Operation(summary = "Crea un producto en el catálogo", description = "Requiere rol ADMIN.")
    @ApiResponse(responseCode = "201", description = "Producto creado correctamente.")
    @ApiResponse(responseCode = "400", description = "Datos del producto inválidos (validación de campos).")
    @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN.")
    @PostMapping("/productos")
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoDTO productoDTO) {
        Producto producto = new Producto(null, productoDTO.nombre(), productoDTO.descripcion(), productoDTO.precio(), productoDTO.categoria(), productoDTO.pesoRollo());
        Producto productoCreado = productoService.crear(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoCreado);

    }

    /**
     * PUT /productos/{id} - actualiza un producto existente (solo ADMIN).
     * El id que manda es el de la URL, no el del cuerpo: se fuerza al
     * construir el Producto para que save() haga UPDATE y no un INSERT nuevo.
     *
     * @param id          id del producto a actualizar (de la URL).
     * @param productoDTO datos nuevos, validados con @Valid.
     * @return 200 con el producto actualizado, o 404 si ese id no existe.
     */
    @Operation(summary = "Actualiza un producto existente", description = "Requiere rol ADMIN.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente.")
    @ApiResponse(responseCode = "400", description = "Datos del producto inválidos (validación de campos).")
    @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN.")
    @ApiResponse(responseCode = "404", description = "No existe ningún producto con ese id.")
    @PutMapping("/productos/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoDTO productoDTO) {
        Producto producto = new Producto(id, productoDTO.nombre(), productoDTO.descripcion(), productoDTO.precio(), productoDTO.categoria(), productoDTO.pesoRollo());
        Producto pro = productoService.actualizar(id, producto);
        if (pro != null) {
            return ResponseEntity.ok(pro);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * DELETE /productos/{id} - borra un producto (solo ADMIN).
     * ResponseEntity<Void> porque ni en éxito ni en fallo hay JSON que devolver.
     *
     * @param id id del producto a borrar (de la URL).
     * @return 204 sin contenido si se ha borrado; 404 si no existía.
     */
    @Operation(summary = "Borra un producto del catálogo", description = "Requiere rol ADMIN.")
    @ApiResponse(responseCode = "204", description = "Producto borrado correctamente.")
    @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN.")
    @ApiResponse(responseCode = "404", description = "No existe ningún producto con ese id.")
    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (productoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}

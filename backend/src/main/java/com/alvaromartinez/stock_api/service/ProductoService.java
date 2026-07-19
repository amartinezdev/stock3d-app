package com.alvaromartinez.stock_api.service;

import com.alvaromartinez.stock_api.model.Categoria;
import com.alvaromartinez.stock_api.model.Producto;
import com.alvaromartinez.stock_api.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


/**
 * Capa de lógica de negocio de productos. No sabe nada de HTTP (nada de
 * ResponseEntity, HttpStatus, @PathVariable/@RequestBody aquí - eso es
 * exclusivo de ProductoController). Habla con ProductoRepository para leer/
 * escribir filas; el controller nunca llama al repository directamente.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }


    /**
     * Lista productos paginados, con filtro opcional por categoría.
     *
     * @param categoria null si no se filtra (usa findAll); si viene, se usa
     *                  el método derivado findByCategoria.
     * @param pageable  página/tamaño/orden pedidos en la URL.
     * @return la página de productos con sus metadatos.
     */
    public Page<Producto> listar(Categoria categoria, Pageable pageable) {
        if (categoria == null) {
            return productoRepository.findAll(pageable);
        } else {
            return productoRepository.findByCategoria(categoria, pageable);
        }

    }

    /**
     * @param id id del producto buscado.
     * @return el producto, o null si no existe (el Optional se desenvuelve
     * aquí con orElse(null) para no exponerlo fuera del Service).
     */
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }


    /**
     * Guarda un producto nuevo del catálogo. La antigua validación de "stock
     * no negativo" ya no va aquí: el stock es por usuario y esa regla vive
     * ahora en InventarioService (abrirRollo, consumirGramos, venta).
     *
     * @param producto entidad ya montada por el controller desde el DTO.
     * @return el producto guardado, con su id generado.
     */
    public Producto crear(Producto producto) {
        return productoRepository.save(producto);
    }


    /**
     * @param id       id del producto a actualizar (el de la URL, no el del cuerpo).
     * @param producto datos nuevos.
     * @return el producto actualizado, o null si ese id no existe (404 en el controller).
     */
    public Producto actualizar(Long id, Producto producto) {
        if (productoRepository.existsById(id)) {
            // Se fuerza el id (de la URL, no del cuerpo) antes de guardar:
            // si no, save() interpretaría un id nulo como "insertar nuevo" en
            // vez de "actualizar el existente".
            producto.setId(id);
            productoRepository.save(producto);
            return producto;
        }
        // null aquí = "no existe ese id" - el controller lo traduce a 404.
        return null;
    }


    /**
     * @param id id del producto a borrar.
     * @return true si existía y se ha borrado; false si no existía (404 en el controller).
     */
    public boolean eliminar(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

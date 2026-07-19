package com.alvaromartinez.stock_api.repository;

import com.alvaromartinez.stock_api.model.Inventario;
import com.alvaromartinez.stock_api.model.Producto;
import com.alvaromartinez.stock_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository de Inventario: Spring Data JPA genera la implementación sola.
 */
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    /**
     * Derivado por nombre: WHERE usuario_id = ? AND producto_id = ?.
     * La búsqueda clave del Service: saber si el usuario ya tiene fila de
     * este producto (para sumar) o hay que crearla.
     *
     * @param usuario  dueño del inventario.
     * @param producto producto buscado.
     * @return la fila de ese par usuario+producto, o Optional vacío.
     */
    Optional<Inventario> findByUsuarioAndProducto(Usuario usuario, Producto producto);
}

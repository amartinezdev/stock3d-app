package com.alvaromartinez.stock_api.repository;

import com.alvaromartinez.stock_api.model.Categoria;
import com.alvaromartinez.stock_api.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository de Producto: Spring Data JPA genera la implementación solo,
 * nunca la escribimos a mano. Ya vienen gratis findAll(), findById(),
 * save(), deleteById(), existsById()...
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Método derivado por nombre: Spring lo traduce a un SELECT con
     * WHERE categoria = ? más el LIMIT/OFFSET de la paginación.
     *
     * @param categoria categoría por la que filtrar.
     * @param pageable  página/tamaño/orden pedidos en la URL.
     * @return la página de productos de esa categoría, con metadatos.
     */
    Page<Producto> findByCategoria(Categoria categoria, Pageable pageable);
}

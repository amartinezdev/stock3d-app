package com.alvaromartinez.stock_api.repository;

import com.alvaromartinez.stock_api.model.EnUso;
import com.alvaromartinez.stock_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository de EnUso. Aparte de lo que JpaRepository da gratis (findById()
 * para localizar el rollo abierto en consumir, y save()), necesita listar
 * los rollos abiertos de un usuario para la pantalla de "en uso".
 */
public interface EnUsoRepository extends JpaRepository<EnUso, Long> {

    /**
     * Rollos abiertos de un usuario, del más reciente al más antiguo. Se
     * desempata por id descendente porque fechaApertura es solo el día: dos
     * rollos abiertos hoy tendrían la misma fecha y un orden impredecible.
     *
     * @param usuario dueño de los rollos.
     * @return sus filas de EnUso (incluidas las ya agotadas, gramos = 0).
     */
    List<EnUso> findByUsuarioOrderByFechaAperturaDescIdDesc(Usuario usuario);
}

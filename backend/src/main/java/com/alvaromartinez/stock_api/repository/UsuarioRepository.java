package com.alvaromartinez.stock_api.repository;

import com.alvaromartinez.stock_api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository de Usuario. Los métodos de abajo son "derivados por nombre":
 * Spring lee el nombre (findBy + campo de la entidad) y genera la consulta
 * solo, sin SQL a mano. No es magia: si el nombre no coincide EXACTO con un
 * campo de Usuario, la app falla al arrancar.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * SELECT ... WHERE user_name = ? — usado en login, registro y para
     * resolver el usuario autenticado en los controllers.
     *
     * @param userName nombre de usuario a buscar.
     * @return el usuario si existe; Optional vacío si no.
     */
    Optional<Usuario> findByUserName(String userName);

    /**
     * SELECT ... WHERE email = ? — usado en el registro para no duplicar emails.
     *
     * @param email email a buscar.
     * @return el usuario si existe; Optional vacío si no.
     */
    Optional<Usuario> findByEmail(String email);
}

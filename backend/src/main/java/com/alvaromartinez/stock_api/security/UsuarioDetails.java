package com.alvaromartinez.stock_api.security;

import com.alvaromartinez.stock_api.model.Usuario;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Traductor: envuelve un Usuario real y expone lo que Spring Security sabe
 * leer (implementa la interfaz UserDetails de la librería, no una clase
 * propia). Se creó para conectar Spring Security con la tabla Usuario a
 * través de UserDetailsService/AuthenticationManager (HTTP Basic).
 *
 * NOTA (estado actual): desde que el login usa JWT y UsuarioService.verificar
 * comprueba la contraseña a mano (sin pasar por AuthenticationManager), y con
 * httpBasic() desactivado en SecurityConfig, esta clase ya NO se usa en el
 * flujo real de la app. Queda aquí como código huérfano hasta decidir si se
 * borra o se conserva.
 */
public class UsuarioDetails implements UserDetails {

    private Usuario user;

    public UsuarioDetails(Usuario user) {
        this.user = user;
    }

    // Los roles llevan el prefijo "ROLE_" por convención de Spring Security
    // (lo espera así en varios sitios internos, como hasRole("ADMIN")).
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().name()));
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    // Los 4 métodos de abajo controlan caducidad/bloqueo de cuenta - como no
    // se implementó esa funcionalidad, siempre se devuelve true (ninguna
    // restricción de este tipo se aplica nunca).
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

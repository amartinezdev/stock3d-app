package com.alvaromartinez.stock_api.security;

import com.alvaromartinez.stock_api.model.Usuario;
import com.alvaromartinez.stock_api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Busca un Usuario (con UsuarioRepository) y lo entrega envuelto en
 * UsuarioDetails. Antes de JWT, Spring Security llamaba a este método
 * automáticamente en cada intento de autenticación con HTTP Basic.
 *
 * NOTA (estado actual): igual que UsuarioDetails, esta clase ya NO se invoca
 * en el flujo real - httpBasic() está desactivado y el login pasa por
 * UsuarioService.verificar(...), que no usa AuthenticationManager ni
 * UserDetailsService. Código huérfano por ahora.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * @param username el nombre de usuario que Spring Security quiere cargar.
     * @return el Usuario de la BBDD envuelto en UsuarioDetails.
     * @throws UsernameNotFoundException si no existe ese userName.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario no se encuentra"));
        return new UsuarioDetails(user);
    }
}

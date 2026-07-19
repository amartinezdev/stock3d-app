package com.alvaromartinez.stock_api.service;

import com.alvaromartinez.stock_api.dto.LoginDTO;
import com.alvaromartinez.stock_api.dto.RegistroDTO;
import com.alvaromartinez.stock_api.dto.TokenResponseDTO;
import com.alvaromartinez.stock_api.dto.UsuarioResponseDTO;
import com.alvaromartinez.stock_api.model.Rol;
import com.alvaromartinez.stock_api.model.Usuario;
import com.alvaromartinez.stock_api.repository.UsuarioRepository;
import com.alvaromartinez.stock_api.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Lógica de negocio de registro y login. Tres colaboradores inyectados:
 * UsuarioRepository (leer/guardar filas), PasswordEncoder (hashear/comparar
 * contraseñas) y JwtUtil (generar tokens). Igual que ProductoService, no
 * sabe nada de HTTP.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwt;


    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwt) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
    }

    /**
     * Registra un usuario nuevo. A diferencia del login, aquí sí se informa
     * de forma específica qué campo está duplicado (userName o email) - no
     * hay el mismo riesgo de "regalar información a un atacante" que en el
     * login, porque registrar no es un intento de acceso a una cuenta ajena.
     * El rol se fija siempre a USER aquí, en el backend - RegistroDTO ni
     * siquiera tiene un campo "rol", así que el cliente no puede
     * auto-asignarse ADMIN de ninguna forma.
     */
    public UsuarioResponseDTO registrar(RegistroDTO registroDTO) {
        Optional<Usuario> user = usuarioRepository.findByUserName(registroDTO.userName());
        Optional<Usuario> email = usuarioRepository.findByEmail(registroDTO.email());

        if (user.isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe.");
        } else if (email.isPresent()) {
            throw new IllegalArgumentException("El email ya existe.");
        }


        // Se hashea ANTES de guardar - nunca se guarda la contraseña en
        // texto plano en ningún punto intermedio.
        String pw = passwordEncoder.encode(registroDTO.password());

        // Usuario solo tiene constructor vacío (a diferencia de Producto), así
        // que se construye con setters uno a uno en vez de un constructor con
        // todos los campos.
        Usuario usuario = new Usuario();
        usuario.setNombre(registroDTO.nombre());
        usuario.setEmail(registroDTO.email());
        usuario.setUserName(registroDTO.userName());
        usuario.setPassword(pw);
        usuario.setRol(Rol.USER);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Se devuelve UsuarioResponseDTO, no la entidad Usuario directamente:
        // así el hash de la contraseña nunca sale en la respuesta de la API.
        return new UsuarioResponseDTO(usuarioGuardado.getId(), usuarioGuardado.getNombre(), usuarioGuardado.getEmail(), usuarioGuardado.getUserName(), usuarioGuardado.getRol());

    }

    /**
     * Verifica credenciales y genera un token JWT si son correctas. Si el
     * usuario no existe, O la contraseña no coincide, se lanza la MISMA
     * excepción con el MISMO mensaje genérico en los dos casos - a
     * propósito, para no revelar desde fuera cuál de las dos cosas falló (no
     * regalar qué userName existen en el sistema).
     */
    public TokenResponseDTO verificar(LoginDTO login) {
        Optional<Usuario> user = usuarioRepository.findByUserName(login.userName());

        if (user.isEmpty()) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
        }

        Usuario usuario = user.get();

        // matches (no encode): compara la contraseña en texto plano recibida
        // contra el hash ya guardado, sin necesidad de "deshacer" el hash
        // (un hash no es reversible).
        if (!passwordEncoder.matches(login.password(), usuario.getPassword())) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
        }

        // Se usan el username y el rol REALES del Usuario ya guardado, no
        // ningún dato que mande el cliente en el LoginDTO - el rol del token
        // siempre sale de lo que ya hay en la base de datos.
        String token = jwt.generarToken(usuario.getUserName(), usuario.getRol().toString());

        return new TokenResponseDTO(token);
    }


}

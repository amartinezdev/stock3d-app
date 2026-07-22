package com.alvaromartinez.stock_api.service;

import com.alvaromartinez.stock_api.dto.LoginDTO;
import com.alvaromartinez.stock_api.dto.RegistroDTO;
import com.alvaromartinez.stock_api.dto.TokenResponseDTO;
import com.alvaromartinez.stock_api.dto.UsuarioResponseDTO;
import com.alvaromartinez.stock_api.model.Rol;
import com.alvaromartinez.stock_api.model.Usuario;
import com.alvaromartinez.stock_api.repository.UsuarioRepository;
import com.alvaromartinez.stock_api.security.JwtUtil;
import org.apache.juli.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwt;

    @InjectMocks
    private UsuarioService usuarioService;


    @Test
    void registrar_ok() {
        RegistroDTO dto = new RegistroDTO("Ana", "ana@mail.com", "ana99", "1234");

        when(usuarioRepository.findByUserName(dto.userName())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("hashFalso");

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setEmail(dto.email());
        usuarioGuardado.setNombre(dto.nombre());
        usuarioGuardado.setUserName(dto.userName());

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);

        UsuarioResponseDTO user = usuarioService.registrar(dto);

        assertEquals(usuarioGuardado.getUserName(), user.userName());
        assertEquals(usuarioGuardado.getEmail(), user.email());
        assertEquals(usuarioGuardado.getNombre(), user.nombre());
    }

    @Test
    void registrar_ExcepcionUsername() {
        RegistroDTO dto = new RegistroDTO("Ana", "ana@mail.com", "ana99", "1234");
        Usuario user = new Usuario();

        when(usuarioRepository.findByUserName(dto.userName())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                usuarioService.registrar(dto));
    }

    @Test
    void registrar_ExcepcionEmail() {
        RegistroDTO dto = new RegistroDTO("Ana", "ana@mail.com", "ana99", "1234");
        Usuario user = new Usuario();

        when(usuarioRepository.findByEmail(dto.email())).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                usuarioService.registrar(dto));
    }

    @Test
    void verificar_usuarioNoExiste() {
        LoginDTO login = new LoginDTO("Hola", "123");

        when(usuarioRepository.findByUserName(login.userName())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.verificar(login));
    }

    @Test
    void verificar_pwNoCoincide() {
        LoginDTO login = new LoginDTO("Hola", "123");
        Usuario user = new Usuario();


        when(usuarioRepository.findByUserName(login.userName())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(login.password(), user.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.verificar(login));
    }

    @Test
    void verificar_ok() {
        LoginDTO login = new LoginDTO("Hola", "123");
        Usuario user = new Usuario();
        user.setUserName(login.userName());
        user.setRol(Rol.USER);

        when(usuarioRepository.findByUserName(login.userName())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(login.password(), user.getPassword())).thenReturn(true);

        when(jwt.generarToken(login.userName(), user.getRol().toString())).thenReturn("tokenFalso");
        TokenResponseDTO resultado = usuarioService.verificar(login);

        assertEquals(resultado.token(), "tokenFalso");
    }

}

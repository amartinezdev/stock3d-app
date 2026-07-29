package com.alvaromartinez.stock_api.controller;

import com.alvaromartinez.stock_api.dto.LoginDTO;
import com.alvaromartinez.stock_api.dto.RegistroDTO;
import com.alvaromartinez.stock_api.dto.TokenResponseDTO;
import com.alvaromartinez.stock_api.dto.UsuarioResponseDTO;
import com.alvaromartinez.stock_api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticación: registro y login. Son los dos únicos SIN
 * token (permitAll en SecurityConfig) - sin ellos nadie podría conseguir
 * su primer JWT. Toda la lógica vive en UsuarioService.
 */
@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;

    // Spring inyecta aquí el bean de UsuarioService ya creado por él;
    // este constructor no lo crea, solo lo recibe y lo guarda en el campo.
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * POST /registrar - crea una cuenta nueva. El rol no viaja en el DTO:
     * lo fija siempre el backend como USER.
     *
     * @param registroDTO nombre, email, userName y password; @Valid dispara
     *                    sus validaciones (@NotBlank, @Email) antes de entrar.
     * @return 201 (creado) con el usuario guardado como UsuarioResponseDTO,
     * sin el hash de la contraseña.
     */
    @Operation(summary = "Registra una cuenta nueva", description = "El rol se asigna siempre como USER; no se puede elegir desde el cliente.")
    @ApiResponse(responseCode = "201", description = "Usuario creado correctamente.")
    @ApiResponse(responseCode = "400", description = "Datos inválidos, o el userName/email ya existen.")
    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponseDTO> registrar(@Valid @RequestBody RegistroDTO registroDTO) {
        UsuarioResponseDTO user = usuarioService.registrar(registroDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * POST /login - comprueba credenciales y devuelve el JWT. POST (y no GET)
     * porque la contraseña debe ir en el cuerpo, nunca en la URL. La
     * verificación real (buscar usuario, PasswordEncoder.matches, generar el
     * token) vive en UsuarioService.verificar - y falla con el MISMO mensaje
     * genérico tanto si el usuario no existe como si la contraseña no
     * coincide, para no revelar qué userName existen.
     *
     * @param loginDTO userName y password en texto plano, validados con @Valid.
     * @return 200 con el token JWT (TokenResponseDTO).
     */
    @Operation(summary = "Inicia sesión y devuelve un JWT", description = "El mensaje de error es el mismo tanto si el usuario no existe como si la contraseña es incorrecta, para no revelar qué userName existen.")
    @ApiResponse(responseCode = "200", description = "Login correcto; el token va en el cuerpo de la respuesta.")
    @ApiResponse(responseCode = "400", description = "Usuario o contraseña incorrectos, o campos vacíos.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> loguear(@Valid @RequestBody LoginDTO loginDTO) {
        TokenResponseDTO token = usuarioService.verificar(loginDTO);
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }
}

package com.alvaromartinez.stock_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que conecta el JWT con Spring Security en cada petición. Extiende
 * OncePerRequestFilter (se ejecuta una vez por petición) y se registra en
 * SecurityConfig con .addFilterBefore(...), antes del filtro estándar de
 * usuario/contraseña.
 * Se comunica con JwtUtil (validar/leer el token) y con el
 * SecurityContextHolder de Spring Security (para marcar la petición como
 * autenticada).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jw;

    public JwtAuthFilter(JwtUtil jw) {
        this.jw = jw;
    }

    /**
     * Punto de diseño clave: este filtro NUNCA rechaza la petición él mismo.
     * Si no hay cabecera, no empieza por "Bearer ", o el token no es válido,
     * simplemente no marca nada y deja pasar la petición igual (si cortara
     * aquí, /registrar y /login, que no llevan token, dejarían de funcionar).
     * Quien de verdad rechaza, más adelante en la cadena, es la regla
     * .anyRequest().authenticated() de SecurityConfig, al no encontrar
     * ninguna autenticación guardada en el SecurityContextHolder -> 401.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        
        if (header != null && header.startsWith("Bearer ")) {
            // "Bearer " tiene 7 caracteres (con el espacio) -> se descarta ese
            // prefijo y se queda solo con el token en sí.
            String token = header.substring(7);
            if (jw.validarToken(token)) {
                String userName = jw.extraerUsername(token);
                String rol = jw.extraerRol(token);

                // UsernamePasswordAuthenticationToken: el objeto que Spring
                // Security entiende como "prueba de autenticación". null en
                // el 2º parámetro porque no hace falta contraseña (ya se
                // verificó vía la firma del token). El rol se envuelve como
                // SimpleGrantedAuthority con el prefijo "ROLE_" (convención
                // que Spring Security espera internamente, p. ej. en
                // hasRole("ADMIN")), para poder autorizar por rol en SecurityConfig.
                Authentication auth = new UsernamePasswordAuthenticationToken(userName, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                SecurityContextHolder.getContext().setAuthentication(auth);

            }
        }

        // Se ejecuta SIEMPRE, autenticado o no - ver nota de diseño arriba.
        filterChain.doFilter(request, response);
    }
}

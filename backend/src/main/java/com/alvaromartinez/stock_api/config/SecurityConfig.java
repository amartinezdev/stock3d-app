package com.alvaromartinez.stock_api.config;

import java.util.List;
import com.alvaromartinez.stock_api.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración central de Spring Security para toda la app. Sin esto (solo
 * con spring-boot-starter-security en el pom.xml), Spring Security protegería
 * TODO por defecto con un usuario aleatorio generado en consola - esta clase
 * sustituye ese comportamiento por el propio (JWT + reglas por ruta).
 */
@Configuration
public class SecurityConfig {

    /**
     * Se registra con {@code @Bean} (y no con @Component) porque
     * BCryptPasswordEncoder es de una librería: no podemos anotar su clase,
     * así que lo registramos desde un método.
     *
     * @return el encoder BCrypt que usa UsuarioService para hashear/comparar.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * La cadena de seguridad completa: qué rutas son públicas, cuáles piden
     * rol, y dónde se engancha nuestro filtro JWT.
     *
     * @param http          el builder de configuración que da Spring Security.
     * @param jwtAuthFilter se inyecta solo porque JwtAuthFilter es @Component.
     * @return la SecurityFilterChain ya construida.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                // CSRF protege sesiones basadas en cookies; con JWT (sin estado,
                // sin cookies de sesión) ese ataque no aplica.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // HTTP Basic ya no hace falta: JWT es lo que sustituye a la
                // autenticación de usuario/contraseña en cada petición. Se
                // desactiva explícitamente para no tener dos mecanismos de
                // autenticación compitiendo a la vez.
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas, sin autenticación - deben ir ANTES de
                        // anyRequest().authenticated(), porque Spring Security
                        // evalúa las reglas en orden y se queda con la primera
                        // que haga match.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
                        .requestMatchers("/registrar", "/login", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Restricción por ROL, no solo por ruta: mismo path que el
                        // GET de listar/obtener (abierto a cualquier autenticado más
                        // abajo), pero solo ADMIN puede crear/actualizar/borrar
                        // productos. hasRole("ADMIN") busca la authority "ROLE_ADMIN"
                        // - la misma que JwtAuthFilter construye a partir del claim
                        // "rol" del token. El path debe coincidir EXACTO con el de
                        // ProductoController (con "/{id}" en PUT/DELETE): si no
                        // coincidiera, la petición "caería" en anyRequest().authenticated()
                        // de abajo, que no comprueba rol - cualquier autenticado pasaría.
                        .requestMatchers(HttpMethod.POST, "/productos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/productos/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/productos/{id}").hasRole("ADMIN")
                        // Cualquier otra petición (el GET de listar/obtener incluido)
                        // solo exige estar autenticado, sin importar el rol.
                        .anyRequest().authenticated()
                )
                // Registra JwtAuthFilter en la cadena de filtros, ANTES del
                // filtro estándar de usuario/contraseña (que ya no se usa,
                // pero sigue existiendo internamente en Spring Security).
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

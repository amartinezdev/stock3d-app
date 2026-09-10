package com.alvaromartinez.stock_api.config;

import java.util.Arrays;
import java.util.List;
import com.alvaromartinez.stock_api.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * CORS: qué webs de OTRO origen pueden llamar a esta API desde un
     * navegador. La lista NO va escrita aquí sino en la propiedad
     * app.cors.origenes (variable de entorno CORS_ORIGENES), porque cambia
     * según dónde esté desplegado el frontend.
     *
     * En producción el frontend se sirve desde el mismo dominio que la API
     * -el proxy inverso manda /api aquí- así que las peticiones son del
     * mismo origen y CORS deja de intervenir: allí la lista puede ir vacía.
     *
     * @param origenes orígenes permitidos separados por comas; vacío = ninguno.
     * @return la configuración CORS aplicada a todas las rutas.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes:}") String origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origenes.split(","))
                .map(String::trim)
                .filter(origen -> !origen.isEmpty())
                .toList());
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
     * @param cors          la configuración CORS del bean de arriba; se recibe
     *                      como parámetro (y no se llama al método) porque
     *                      ese bean necesita que Spring le inyecte su
     *                      propiedad app.cors.origenes. El @Qualifier es
     *                      obligatorio: Spring MVC registra otro bean que
     *                      también implementa CorsConfigurationSource
     *                      (mvcHandlerMappingIntrospector) y sin nombrar
     *                      cuál queremos, la inyección es ambigua.
     * @return la SecurityFilterChain ya construida.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, @Qualifier("corsConfigurationSource") CorsConfigurationSource cors) throws Exception {
        http
                // CSRF protege sesiones basadas en cookies; con JWT (sin estado,
                // sin cookies de sesión) ese ataque no aplica.
                .csrf(csrf -> csrf.disable())
                .cors(configuracion -> configuracion.configurationSource(cors))
                
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

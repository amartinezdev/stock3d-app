package com.alvaromartinez.stock_api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

/**
 * Genera y valida los JWT (JSON Web Token) que reemplazan a las sesiones
 * clásicas: en vez de que el servidor guarde una sesión, toda la info
 * necesaria (username, rol, expiración) viaja dentro del propio token,
 * firmado con una clave secreta que solo el servidor conoce. "Stateless":
 * no se guarda nada de esto en BBDD ni en memoria.
 */
@Component
public class JwtUtil {

    private final SecretKey key;

    // @Value("${jwt.secret}") - Spring inyecta aquí el valor de la propiedad
    // jwt.secret de application.properties (32+ caracteres: HS256 exige esa
    // longitud mínima o lanza WeakKeyException).
    // Keys.hmacShaKeyFor(...) convierte ese String (como bytes) en un SecretKey
    // real, usado tanto para firmar como para verificar.
    public JwtUtil(@Value("${jwt.secret}") String claveSecreta) {
        this.key = Keys.hmacShaKeyFor(claveSecreta.getBytes());
    }

    /**
     * Construye un token nuevo firmado. El payload lleva el username como
     * "subject" (el campo estándar de a quién pertenece), el rol como claim
     * personalizado, y la expiración (ahora + 7 días). signWith(key) firma
     * todo con la clave secreta: sin ella es inviable falsificar la firma,
     * aunque el payload sea legible por cualquiera (solo va en Base64).
     *
     * @param username a quién pertenece el token.
     * @param rol      rol del usuario ("ADMIN"/"USER"), viaja dentro del token.
     * @return el JWT completo (header.payload.signature) como String.
     */
    public String generarToken(String username, String rol) {
        String token = Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .expiration(new Date(System.currentTimeMillis() + Duration.ofDays(7).toMillis()))
                .signWith(key)
                .compact();

        return token;
    }

    /**
     * Comprueba que el token sea legítimo: firma correcta Y no expirado.
     * parseSignedClaims hace las dos comprobaciones a la vez y lanza
     * JwtException si falla cualquiera - la capturamos y la convertimos en
     * un simple true/false para que no rompa la petición.
     *
     * @param token el JWT tal como llega en la cabecera (sin el "Bearer ").
     * @return true si es válido; false si la firma no cuadra o ha expirado.
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Lee el username de dentro del token (verificando la firma de paso).
     * getSubject() es la contrapartida de lectura del .subject(username)
     * que pusimos al generar.
     *
     * @param token el JWT a leer.
     * @return el username guardado como subject.
     */
    public String extraerUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Lee el rol guardado como claim personalizado del token. El segundo
     * argumento de get() le dice a jjwt "comprueba que es un String y
     * dámelo ya como tal" (lectura tipada, sin casts).
     *
     * @param token el JWT a leer.
     * @return el rol guardado en el claim "rol" ("ADMIN"/"USER").
     */
    public String extraerRol(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("rol", String.class);
    }
}

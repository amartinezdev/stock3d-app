package com.alvaromartinez.stock_api.model;

import jakarta.persistence.*;

/**
 * Entidad JPA: la fila real de la tabla "usuario", con el password ya
 * hasheado. No confundir con RegistroDTO (entrada) ni UsuarioResponseDTO
 * (salida), que son el contrato de la API.
 */
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    // unique = true: la propia BBDD rechaza duplicados, como segunda barrera
    // por si fallara la comprobación que ya hace UsuarioService.registrar.
    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String userName;

    // STRING guarda "ADMIN"/"USER" como texto, no la posición numérica del
    // enum - así reordenar Rol en el futuro no corrompe lo ya guardado.
    @Enumerated(EnumType.STRING)
    private Rol rol;

    // Siempre el hash de BCrypt, nunca la contraseña en texto plano.
    private String password;


    // Solo constructor vacío (obligatorio para Hibernate); por eso
    // UsuarioService monta el Usuario con setters uno a uno.
    public Usuario() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

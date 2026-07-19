package com.alvaromartinez.stock_api.model;

/**
 * Roles posibles de un usuario. Enum en vez de String libre: así es
 * imposible guardar un rol inventado (el compilador lo impide).
 * Usuario.rol lo persiste como texto con @Enumerated(EnumType.STRING).
 */
public enum Rol {
    ADMIN,
    USER
}

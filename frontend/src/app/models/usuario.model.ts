/** Espejo del enum Rol del backend. */
export type Rol = 'ADMIN' | 'USER';

/** Datos públicos de un usuario (UsuarioResponseDTO). Nunca trae contraseña. */
export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  userName: string;
  rol: Rol;
}

export interface LoginPeticion {
  userName: string;
  password: string;
}

export interface RegistroPeticion {
  nombre: string;
  email: string;
  userName: string;
  password: string;
}

/** Respuesta del login: solo el JWT. */
export interface TokenRespuesta {
  token: string;
}

/**
 * Formato común de error de la API (ErrorResponse del backend). Se usa para
 * enseñar el mensaje real del servidor en vez de un "algo ha fallado".
 */
export interface ErrorApi {
  mensaje: string;
  codigo: number;
  fechaHora: string;
}

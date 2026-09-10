import { HttpErrorResponse } from '@angular/common/http';
import { ErrorApi } from '../models/usuario.model';

/**
 * Traduce cualquier fallo HTTP al mensaje que se le enseña a la persona.
 * Se prioriza SIEMPRE el mensaje que manda el backend (ErrorResponse.mensaje:
 * "No puedes vender más de lo que tienes", "El email ya existe"...), porque
 * es el que explica de verdad qué ha pasado; los textos de aquí son solo el
 * plan B para los casos en que no llega cuerpo (red caída, 403, 500...).
 */
export function mensajeDeError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ha ocurrido un error inesperado.';
  }

  const cuerpo = error.error as Partial<ErrorApi> | string | null;

  if (cuerpo && typeof cuerpo === 'object' && typeof cuerpo.mensaje === 'string') {
    return cuerpo.mensaje;
  }

  switch (error.status) {
    case 0:
      // La petición no llegó a salir: red caída, o el navegador la bloqueó
      // (CORS). Con el proxy delante esto ya casi no pasa.
      return 'No se puede conectar con el servidor. Revisa tu conexión.';
    case 401:
      return 'Tu sesión ha caducado. Vuelve a iniciar sesión.';
    case 403:
      return 'No tienes permisos para hacer esto.';
    case 404:
      return 'No se ha encontrado lo que buscabas.';
    case 500:
      return 'Error interno del servidor.';
    case 502:
    case 503:
    case 504:
      // El proxy está en pie pero no encuentra la API detrás: casi siempre
      // es el backend parado o todavía arrancando.
      return 'El servidor no responde ahora mismo. ¿Está arrancado el backend?';
    default:
      return 'Ha ocurrido un error inesperado.';
  }
}

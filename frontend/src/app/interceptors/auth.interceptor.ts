import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/** Rutas públicas: no llevan token y un 401 suyo no debe cerrar sesión. */
const PUBLICAS = ['/login', '/registrar'];

/**
 * Se ejecuta en TODAS las peticiones HTTP de la app. Hace dos cosas:
 *
 * 1. Añade la cabecera "Authorization: Bearer {token}" si hay sesión, para
 *    no tener que acordarse de ponerla en cada servicio (y no olvidarla en
 *    alguno).
 * 2. Si el backend responde 401, el token ya no sirve (caducado o
 *    manipulado): se cierra la sesión y se manda al login. El error se
 *    vuelve a lanzar igualmente para que el componente pueda enseñar su
 *    mensaje si quiere.
 *
 * La petición NO se modifica, se CLONA: HttpRequest es inmutable a
 * propósito, así un interceptor no puede romperle la petición a otro.
 */
export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const esPublica = PUBLICAS.some((ruta) => peticion.url.endsWith(ruta));

  const conToken =
    token && !esPublica
      ? peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : peticion;

  return siguiente(conToken).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !esPublica) {
        auth.expulsarPorTokenInvalido();
      }

      return throwError(() => error);
    }),
  );
};

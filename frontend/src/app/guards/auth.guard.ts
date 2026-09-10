import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Portero de las rutas privadas. Tres casos:
 *
 * - Sin token: fuera, al login (guardando a dónde quería ir).
 * - Con token y perfil ya cargado: pasa directamente.
 * - Con token pero sin perfil (recarga de página, F5): se pide /me y se
 *   espera su respuesta. Si el backend lo acepta, pasa; si devuelve error,
 *   ese token ya no vale y toca volver al login.
 *
 * Devolver un UrlTree en vez de hacer router.navigate() es lo recomendado
 * en Angular: cancela la navegación actual y redirige en un solo paso, sin
 * dejar a medias la ruta que se estaba activando.
 */
export const authGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const alLogin = () =>
    router.createUrlTree(['/login'], { queryParams: { volverA: estado.url } });

  if (!auth.haySesion()) {
    return alLogin();
  }

  if (auth.usuario()) {
    return true;
  }

  return auth.cargarPerfil().pipe(
    map(() => true),
    catchError(() => of(alLogin())),
  );
};

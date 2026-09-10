import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * El contrario de authGuard: protege /login y /registro de quien YA tiene
 * sesión. Sin esto, alguien logueado podría volver al formulario de login
 * con el botón de atrás, lo cual solo confunde.
 */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.haySesion() ? router.createUrlTree(['/panel']) : true;
};

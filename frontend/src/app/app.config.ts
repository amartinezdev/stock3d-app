import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter, withComponentInputBinding, withInMemoryScrolling } from '@angular/router';
import { routes } from './app.routes';
import { authInterceptor } from './interceptors/auth.interceptor';

// Los pipes date, currency y number traen solo el inglés de serie. Esto
// carga los datos del español (coma decimal, "10 sept 2026", el símbolo €
// detrás del número) y hay que hacerlo antes de arrancar la aplicación.
registerLocaleData(localeEs, 'es-ES');

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(
      routes,
      // Ata los parámetros de la ruta a los input() de los componentes.
      withComponentInputBinding(),
      // Al cambiar de pantalla se sube arriba del todo, como haría una web
      // normal; el navegador solo restaura el scroll al ir atrás.
      withInMemoryScrolling({ scrollPositionRestoration: 'enabled', anchorScrolling: 'enabled' }),
    ),
    // El interceptor va aquí, en la configuración de HttpClient: así se
    // aplica a TODAS las peticiones de la app sin tocar ni un servicio.
    provideHttpClient(withInterceptors([authInterceptor])),
    { provide: LOCALE_ID, useValue: 'es-ES' },
  ],
};

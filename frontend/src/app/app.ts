import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Avisos } from './components/avisos';
import { TemaService } from './services/tema.service';

/**
 * Componente raíz. A propósito no tiene casi nada: solo el hueco donde el
 * router pinta la pantalla que toque y los avisos flotantes, que van aquí
 * para poder aparecer por encima de cualquier pantalla.
 *
 * TemaService se inyecta aunque no se use en la plantilla: al crearse pone
 * en marcha el effect que mantiene sincronizada la clase "dark" del <html>.
 */
@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, Avisos],
  template: `
    <router-outlet />
    <app-avisos />
  `,
})
export class App {
  constructor() {
    // Basta con pedirlo para que exista: su effect se encarga del resto.
    inject(TemaService);
  }
}

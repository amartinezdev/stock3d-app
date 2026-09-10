import { effect, Service, signal } from '@angular/core';

export type Tema = 'oscuro' | 'claro';

const CLAVE = 'stock3d.tema';

/**
 * Tema claro/oscuro. La clase "dark" del <html> es la que activa el bloque
 * de tokens oscuros de styles.css; aquí solo se pone y se quita.
 *
 * El valor inicial lo aplica un script diminuto en index.html ANTES de que
 * arranque Angular: si se esperase a este servicio, la página se pintaría
 * un instante en claro y luego saltaría a oscuro.
 */
@Service()
export class TemaService {
  private readonly _tema = signal<Tema>(leerTemaGuardado());

  readonly tema = this._tema.asReadonly();

  constructor() {
    // effect se vuelve a ejecutar solo cada vez que cambia la señal: no hay
    // que acordarse de tocar el DOM y el localStorage en cada sitio donde
    // se cambie el tema.
    effect(() => {
      const tema = this._tema();
      document.documentElement.classList.toggle('dark', tema === 'oscuro');

      try {
        localStorage.setItem(CLAVE, tema);
      } catch {
        // Sin persistencia: el tema durará hasta recargar.
      }
    });
  }

  alternar(): void {
    this._tema.update((actual) => (actual === 'oscuro' ? 'claro' : 'oscuro'));
  }
}

function leerTemaGuardado(): Tema {
  try {
    return localStorage.getItem(CLAVE) === 'claro' ? 'claro' : 'oscuro';
  } catch {
    return 'oscuro';
  }
}

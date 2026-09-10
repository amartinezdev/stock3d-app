import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Icono } from '../components/icono';
import { AuthService } from '../services/auth.service';
import { TemaService } from '../services/tema.service';

interface Enlace {
  ruta: string;
  texto: string;
  icono: string;
}

/**
 * El "marco" de la parte privada de la app: barra lateral con la
 * navegación, cabecera superior y, en el hueco central, el <router-outlet>
 * donde el router va metiendo cada pantalla.
 *
 * Es el componente de la ruta padre en app.routes.ts, así que se monta una
 * sola vez: al cambiar de sección solo se reemplaza lo de dentro del
 * outlet, la barra lateral ni se repinta.
 */
@Component({
  selector: 'app-layout',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Icono],
  templateUrl: './layout.html',
  styleUrl: './layout.css',
})
export class Layout {
  protected readonly auth = inject(AuthService);
  protected readonly tema = inject(TemaService);

  /** En móvil la barra lateral se esconde y se abre con el botón de menú. */
  protected readonly menuAbierto = signal(false);

  protected readonly enlaces: readonly Enlace[] = [
    { ruta: '/panel', texto: 'Panel', icono: 'panel' },
    { ruta: '/inventario', texto: 'Inventario', icono: 'capas' },
    { ruta: '/en-uso', texto: 'En uso', icono: 'bobina' },
    { ruta: '/productos', texto: 'Catálogo', icono: 'catalogo' },
    { ruta: '/movimientos', texto: 'Movimientos', icono: 'historial' },
  ];
}

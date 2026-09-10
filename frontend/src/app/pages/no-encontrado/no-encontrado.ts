import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icono } from '../../components/icono';

/** Pantalla para cualquier URL que no exista (la ruta comodín ** ). */
@Component({
  selector: 'app-no-encontrado',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Icono],
  template: `
    <div class="tarjeta">
      <div class="vacio">
        <span class="vacio-icono">
          <app-icono nombre="buscar" tamano="lg" />
        </span>
        <h3>Esta página no existe</h3>
        <p>El enlace que has seguido no lleva a ninguna parte de la aplicación.</p>
        <a class="btn btn-primario" style="margin-top: 0.5rem" routerLink="/panel">
          <app-icono nombre="panel" tamano="sm" />
          Volver al panel
        </a>
      </div>
    </div>
  `,
})
export class NoEncontrado {}

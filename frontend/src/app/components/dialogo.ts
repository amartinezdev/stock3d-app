import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  input,
  output,
  viewChild,
} from '@angular/core';
import { Icono } from './icono';

/**
 * Ventana modal reutilizable (entrada de stock, vender, confirmar borrado...).
 * El contenido y los botones los pone quien la usa, mediante proyección:
 *
 *   <app-dialogo titulo="Vender" (cerrar)="abierto.set(false)">
 *     ...formulario...
 *     <div pie>...botones...</div>
 *   </app-dialogo>
 *
 * Detalles de accesibilidad y comodidad que trae de serie:
 * - Escape cierra (el listener va en el document: el diálogo solo existe
 *   mientras está abierto, así que no hay riesgo de que responda de fondo).
 * - Al abrirse, el foco salta dentro del diálogo (al primer campo si lo
 *   hay), en vez de quedarse en el botón que lo abrió.
 * - Click en el fondo oscuro cierra, pero NO el click dentro de la caja:
 *   por eso el $event.target === $event.currentTarget, que distingue el
 *   click en el fondo mismo de uno que ha burbujeado desde dentro.
 * - role="dialog" + aria-modal + aria-labelledby para lectores de pantalla.
 */
@Component({
  selector: 'app-dialogo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icono],
  host: { '(document:keydown.escape)': 'cerrar.emit()' },
  template: `
    <div class="dialogo-fondo" (click)="siEsElFondo($event)">
      <div
        #caja
        class="dialogo"
        role="dialog"
        aria-modal="true"
        tabindex="-1"
        [attr.aria-label]="titulo()"
      >
        <div class="dialogo-cabecera">
          <div>
            <h2 class="tarjeta-titulo">{{ titulo() }}</h2>
            @if (descripcion()) {
              <p class="tarjeta-sub">{{ descripcion() }}</p>
            }
          </div>
          <button
            type="button"
            class="btn btn-fantasma btn-icono btn-sm"
            (click)="cerrar.emit()"
            aria-label="Cerrar"
          >
            <app-icono nombre="cerrar" tamano="sm" />
          </button>
        </div>

        <div class="dialogo-cuerpo">
          <ng-content />
        </div>

        <div class="dialogo-pie">
          <ng-content select="[pie]" />
        </div>
      </div>
    </div>
  `,
})
export class Dialogo {
  readonly titulo = input.required<string>();
  readonly descripcion = input<string>('');

  readonly cerrar = output<void>();

  private readonly caja = viewChild.required<ElementRef<HTMLElement>>('caja');

  constructor() {
    // afterNextRender se ejecuta cuando el diálogo ya está en el DOM: antes
    // de eso el elemento no existe todavía y no se le podría dar el foco.
    afterNextRender(() => {
      const elemento = this.caja().nativeElement;
      const primerCampo = elemento.querySelector<HTMLElement>('input, select, textarea');

      (primerCampo ?? elemento).focus();
    });
  }

  protected siEsElFondo(evento: Event): void {
    if (evento.target === evento.currentTarget) {
      this.cerrar.emit();
    }
  }
}

import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { Icono } from './icono';

/**
 * Pie de paginación de las tablas. No sabe nada de HTTP: recibe en qué
 * página está y cuántas hay, y avisa hacia arriba con (irA) cuando se pulsa
 * una flecha. Quien lo usa es el que vuelve a pedir los datos al backend.
 *
 * La página del backend empieza en 0 (page=0 es la primera), pero a la
 * persona se le enseña empezando en 1, que es lo que espera ver.
 */
@Component({
  selector: 'app-paginacion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icono],
  template: `
    <div class="paginacion">
      <span>
        @if (totalElementos() > 0) {
          {{ desde() }}–{{ hasta() }} de {{ totalElementos() }}
        } @else {
          Sin resultados
        }
      </span>

      <div class="fila">
        <span class="oculto-movil">Página {{ pagina() + 1 }} de {{ totalPaginas() || 1 }}</span>
        <button
          type="button"
          class="btn btn-contorno btn-sm btn-icono"
          [disabled]="pagina() === 0"
          (click)="irA.emit(pagina() - 1)"
          aria-label="Página anterior"
        >
          <app-icono nombre="izq" tamano="sm" />
        </button>
        <button
          type="button"
          class="btn btn-contorno btn-sm btn-icono"
          [disabled]="pagina() + 1 >= totalPaginas()"
          (click)="irA.emit(pagina() + 1)"
          aria-label="Página siguiente"
        >
          <app-icono nombre="der" tamano="sm" />
        </button>
      </div>
    </div>
  `,
})
export class Paginacion {
  /** Página actual, contando desde 0 (igual que en el backend). */
  readonly pagina = input.required<number>();
  readonly totalPaginas = input.required<number>();
  readonly totalElementos = input.required<number>();
  readonly tamano = input<number>(10);

  readonly irA = output<number>();

  protected readonly desde = computed(() => this.pagina() * this.tamano() + 1);

  protected readonly hasta = computed(() =>
    Math.min((this.pagina() + 1) * this.tamano(), this.totalElementos()),
  );
}

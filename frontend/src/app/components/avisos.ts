import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { AvisosService } from '../services/avisos.service';
import { Icono } from './icono';

/**
 * Los mensajes flotantes de la esquina inferior derecha. Se pinta una sola
 * vez, en el componente raíz, y muestra lo que haya en AvisosService: así
 * cualquier pantalla puede avisar de algo sin tener que colocar nada en su
 * propia plantilla.
 *
 * role="status" + aria-live: un lector de pantalla lee el aviso cuando
 * aparece, sin robarle el foco a lo que la persona estuviera haciendo.
 */
@Component({
  selector: 'app-avisos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icono],
  template: `
    <div class="avisos" role="status" aria-live="polite">
      @for (aviso of avisosService.avisos(); track aviso.id) {
        <div class="aviso-item" [class]="aviso.tipo">
          <app-icono [nombre]="icono(aviso.tipo)" tamano="sm" />
          <span class="crece">{{ aviso.texto }}</span>
          <button
            type="button"
            class="btn btn-fantasma btn-sm btn-icono"
            (click)="avisosService.cerrar(aviso.id)"
            aria-label="Cerrar aviso"
          >
            <app-icono nombre="cerrar" tamano="sm" />
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    .aviso-item .btn-icono {
      width: 1.5rem;
      height: 1.5rem;
      margin: -0.1rem -0.2rem 0 0;
    }
  `,
})
export class Avisos {
  protected readonly avisosService = inject(AvisosService);

  protected icono(tipo: string): string {
    if (tipo === 'exito') {
      return 'check';
    }

    return tipo === 'error' ? 'alerta' : 'info';
  }
}

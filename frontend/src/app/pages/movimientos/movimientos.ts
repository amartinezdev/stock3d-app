import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Icono } from '../../components/icono';
import { Paginacion } from '../../components/paginacion';
import { ETIQUETAS_TIPO, Movimiento, TipoMovimiento } from '../../models/movimiento.model';
import { Page } from '../../models/page.model';
import { CategoriaPipe } from '../../pipes/categoria.pipe';
import { GramosPipe } from '../../pipes/gramos.pipe';
import { AvisosService } from '../../services/avisos.service';
import { MovimientoService } from '../../services/movimiento.service';
import { mensajeDeError } from '../../utils/errores';

/** Movimientos por página. */
const TAMANO = 12;

/**
 * Histórico completo de stock: quién ha hecho qué y cuándo. Es solo
 * lectura — estas filas las escribe el backend como consecuencia de cada
 * operación y nunca se editan, que es justo lo que las hace útiles para
 * auditar.
 *
 * El filtro por tipo se manda al servidor (?tipo=), no se aplica sobre el
 * array recibido: como la lista viene paginada, filtrar en el cliente solo
 * afectaría a la página actual y los totales mentirían.
 */
@Component({
  selector: 'app-movimientos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Icono, Paginacion, CategoriaPipe, GramosPipe, DecimalPipe, CurrencyPipe, DatePipe],
  templateUrl: './movimientos.html',
  styleUrl: './movimientos.css',
})
export class Movimientos {
  private readonly movimientoService = inject(MovimientoService);
  private readonly avisos = inject(AvisosService);

  protected readonly tamano = TAMANO;

  protected readonly cargando = signal(true);
  protected readonly datos = signal<Page<Movimiento> | null>(null);
  protected readonly pagina = signal(0);
  protected readonly tipo = signal<TipoMovimiento | null>(null);

  protected readonly movimientos = computed(() => this.datos()?.content ?? []);
  protected readonly total = computed(() => this.datos()?.totalElements ?? 0);
  protected readonly totalPaginas = computed(() => this.datos()?.totalPages ?? 0);

  constructor() {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);

    this.movimientoService
      .listar({ pagina: this.pagina(), tamano: TAMANO, tipo: this.tipo() })
      .subscribe({
        next: (pagina) => {
          this.datos.set(pagina);
          this.cargando.set(false);
        },
        error: (fallo: unknown) => {
          this.avisos.error(mensajeDeError(fallo));
          this.cargando.set(false);
        },
      });
  }

  protected filtrarPor(tipo: TipoMovimiento | null): void {
    this.tipo.set(tipo);
    this.pagina.set(0);
    this.cargar();
  }

  protected irAPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.cargar();
  }

  protected etiquetaTipo(tipo: TipoMovimiento): string {
    return ETIQUETAS_TIPO[tipo];
  }

  /** Icono de cada tipo, para reconocer la fila de un vistazo. */
  protected iconoTipo(tipo: TipoMovimiento): string {
    switch (tipo) {
      case 'ENTRADA':
        return 'caja';
      case 'SALIDA_VENTA':
        return 'etiqueta';
      default:
        return 'bobina';
    }
  }

  /** Clase del distintivo: verde entra, ámbar vende, azul consume. */
  protected claseTipo(tipo: TipoMovimiento): string {
    switch (tipo) {
      case 'ENTRADA':
        return 'badge-exito';
      case 'SALIDA_VENTA':
        return 'badge-aviso';
      default:
        return 'badge-info';
    }
  }
}

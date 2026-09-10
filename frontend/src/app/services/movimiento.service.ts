import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import { Movimiento, TipoMovimiento } from '../models/movimiento.model';
import { Page } from '../models/page.model';

export interface FiltroMovimientos {
  pagina?: number;
  tamano?: number;
  tipo?: TipoMovimiento | null;
}

/**
 * Histórico de stock (solo lectura: los movimientos los crea el backend
 * como efecto de cada operación, nunca se dan de alta a mano).
 */
@Service()
export class MovimientoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/movimientos`;

  /**
   * El filtro por tipo se manda al servidor, no se aplica al array que
   * llega: con paginación, filtrar en el cliente solo filtraría la página
   * actual y las cuentas saldrían mal.
   */
  listar(filtro: FiltroMovimientos = {}): Observable<Page<Movimiento>> {
    let params = new HttpParams()
      .set('page', filtro.pagina ?? 0)
      .set('size', filtro.tamano ?? 10)
      .set('sort', 'fecha,desc');

    if (filtro.tipo) {
      params = params.set('tipo', filtro.tipo);
    }

    return this.http.get<Page<Movimiento>>(this.base, { params });
  }
}

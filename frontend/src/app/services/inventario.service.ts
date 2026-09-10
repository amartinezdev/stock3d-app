import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import {
  AbrirRolloPeticion,
  ConsumirPeticion,
  EnUsoItem,
  EntradaPeticion,
  InventarioItem,
  Resumen,
  VentaPeticion,
} from '../models/inventario.model';

/**
 * Stock propio: lecturas (inventario, rollos abiertos, resumen) y las
 * cuatro operaciones que lo modifican (entrada, abrir rollo, consumir,
 * vender). En ninguna se manda el id del usuario: el backend lo saca del
 * JWT, así que desde aquí es literalmente imposible tocar el stock de otra
 * persona.
 */
@Service()
export class InventarioService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/inventario`;

  /* ---------- Lecturas ---------- */

  listarInventario(): Observable<InventarioItem[]> {
    return this.http.get<InventarioItem[]>(this.base);
  }

  listarEnUso(): Observable<EnUsoItem[]> {
    return this.http.get<EnUsoItem[]>(`${this.base}/enUso`);
  }

  resumen(): Observable<Resumen> {
    return this.http.get<Resumen>(`${this.base}/resumen`);
  }

  /* ---------- Operaciones ---------- */

  /** Añade rollos cerrados al inventario. */
  entrada(peticion: EntradaPeticion): Observable<unknown> {
    return this.http.post(`${this.base}/entrada`, peticion);
  }

  /** Pasa un rollo de "cerrado" a "abierto" (-1 inventario, +1 en uso). */
  abrirRollo(peticion: AbrirRolloPeticion): Observable<unknown> {
    return this.http.post(`${this.base}/abrirRollo`, peticion);
  }

  /** Gasta gramos de un rollo ya abierto. */
  consumir(peticion: ConsumirPeticion): Observable<unknown> {
    return this.http.post(`${this.base}/consumir`, peticion);
  }

  /** Vende rollos cerrados al precio indicado. */
  vender(peticion: VentaPeticion): Observable<unknown> {
    return this.http.post(`${this.base}/vender`, peticion);
  }
}

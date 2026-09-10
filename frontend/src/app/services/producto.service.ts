import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import { Categoria } from '../models/categoria.model';
import { Page } from '../models/page.model';
import { Producto, ProductoPeticion } from '../models/producto.model';

/** Opciones del listado paginado del catálogo. */
export interface FiltroProductos {
  pagina?: number;
  tamano?: number;
  categoria?: Categoria | null;
  orden?: string;
}

/**
 * Todas las llamadas al catálogo (/productos) en un único sitio. Los
 * componentes no saben ni la URL ni cómo se montan los parámetros: solo
 * piden "listar" o "crear" y reciben un Observable ya tipado.
 */
@Service()
export class ProductoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/productos`;

  /**
   * GET /productos?page=&size=&categoria=&sort=
   * HttpParams es inmutable: cada .set() devuelve un objeto nuevo, por eso
   * se reasigna la variable en vez de encadenar sobre la misma.
   */
  listar(filtro: FiltroProductos = {}): Observable<Page<Producto>> {
    let params = new HttpParams()
      .set('page', filtro.pagina ?? 0)
      .set('size', filtro.tamano ?? 10)
      .set('sort', filtro.orden ?? 'nombre,asc');

    if (filtro.categoria) {
      params = params.set('categoria', filtro.categoria);
    }

    return this.http.get<Page<Producto>>(this.base, { params });
  }

  obtener(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.base}/${id}`);
  }

  /** Solo ADMIN (el backend responde 403 al resto). */
  crear(producto: ProductoPeticion): Observable<Producto> {
    return this.http.post<Producto>(this.base, producto);
  }

  /** Solo ADMIN. El id viaja en la URL, no en el cuerpo. */
  actualizar(id: number, producto: ProductoPeticion): Observable<Producto> {
    return this.http.put<Producto>(`${this.base}/${id}`, producto);
  }

  /** Solo ADMIN. Responde 204 sin cuerpo, de ahí el <void>. */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}

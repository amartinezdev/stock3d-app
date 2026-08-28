
import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { ProductoModel } from '../models/producto.model';
import { Page } from '../models/page.model';

@Service()
export class ProductoService {

  private http = inject(HttpClient);

  listarProductos() {
    return this.http.get<Page<ProductoModel>>('http://localhost:8080/productos');
  }

}

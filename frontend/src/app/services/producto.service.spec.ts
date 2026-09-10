import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '../api';
import { ProductoService } from './producto.service';

describe('ProductoService', () => {
  let service: ProductoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      // provideHttpClientTesting sustituye el backend real por uno falso:
      // ninguna petición sale de verdad, se inspeccionan aquí.
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ProductoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('se crea el servicio', () => {
    expect(service).toBeTruthy();
  });

  it('pide el catálogo con paginación y filtro de categoría', () => {
    service.listar({ pagina: 2, tamano: 5, categoria: 'PETG' }).subscribe();

    const peticion = http.expectOne(
      (req) =>
        req.url === `${API_URL}/productos` &&
        req.params.get('page') === '2' &&
        req.params.get('size') === '5' &&
        req.params.get('categoria') === 'PETG',
    );

    expect(peticion.request.method).toBe('GET');
    peticion.flush({ content: [], totalElements: 0, totalPages: 0 });
  });

  it('no manda el parámetro categoria cuando no hay filtro', () => {
    service.listar().subscribe();

    const peticion = http.expectOne((req) => req.url === `${API_URL}/productos`);

    expect(peticion.request.params.has('categoria')).toBe(false);
    peticion.flush({ content: [], totalElements: 0, totalPages: 0 });
  });
});

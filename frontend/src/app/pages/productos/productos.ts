import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Dialogo } from '../../components/dialogo';
import { Icono } from '../../components/icono';
import { Paginacion } from '../../components/paginacion';
import { Categoria, CATEGORIAS } from '../../models/categoria.model';
import { Page } from '../../models/page.model';
import { Producto } from '../../models/producto.model';
import { CategoriaPipe } from '../../pipes/categoria.pipe';
import { GramosPipe } from '../../pipes/gramos.pipe';
import { AuthService } from '../../services/auth.service';
import { AvisosService } from '../../services/avisos.service';
import { ProductoService } from '../../services/producto.service';
import { mensajeDeError } from '../../utils/errores';

/** Productos por página que se piden al backend. */
const TAMANO = 8;

/**
 * Catálogo compartido de productos. Ojo con la diferencia respecto al
 * inventario: el catálogo es la FICHA del filamento (nombre, precio de
 * referencia, gramos por rollo) y lo ve todo el mundo; el stock de cada uno
 * es cosa del inventario personal.
 *
 * Crear, editar y borrar solo lo puede hacer un ADMIN. Aquí se esconden los
 * botones si no lo eres, pero eso es solo para no enseñar lo que no puedes
 * usar: quien decide de verdad es el backend, que responde 403 aunque
 * alguien fuerce la petición desde la consola del navegador.
 */
@Component({
  selector: 'app-productos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    Icono,
    Dialogo,
    Paginacion,
    CategoriaPipe,
    GramosPipe,
    CurrencyPipe,
  ],
  templateUrl: './productos.html',
  styleUrl: './productos.css',
})
export class Productos {
  private readonly productoService = inject(ProductoService);
  private readonly avisos = inject(AvisosService);
  private readonly fb = inject(FormBuilder);
  protected readonly auth = inject(AuthService);

  protected readonly categorias = CATEGORIAS;
  protected readonly tamano = TAMANO;

  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly datos = signal<Page<Producto> | null>(null);
  protected readonly pagina = signal(0);
  protected readonly categoria = signal<Categoria | null>(null);

  /** Producto que se está editando; null cuando el diálogo crea uno nuevo. */
  protected readonly editando = signal<Producto | null>(null);
  protected readonly dialogoAbierto = signal(false);
  protected readonly aBorrar = signal<Producto | null>(null);

  protected readonly formulario = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    descripcion: ['', Validators.required],
    precio: [0, [Validators.required, Validators.min(0)]],
    categoria: ['PLA' as Categoria, Validators.required],
    pesoRollo: [1000, [Validators.required, Validators.min(1)]],
  });

  protected readonly productos = computed(() => this.datos()?.content ?? []);
  protected readonly total = computed(() => this.datos()?.totalElements ?? 0);
  protected readonly totalPaginas = computed(() => this.datos()?.totalPages ?? 0);

  constructor() {
    this.cargar();
  }

  /* ---------- Carga y filtros ---------- */

  protected cargar(): void {
    this.cargando.set(true);

    this.productoService
      .listar({ pagina: this.pagina(), tamano: TAMANO, categoria: this.categoria() })
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

  protected cambiarCategoria(valor: string): void {
    // Al cambiar el filtro se vuelve siempre a la página 0: si estabas en
    // la 3 y el filtro nuevo solo tiene una, te quedarías mirando un vacío.
    this.categoria.set(valor ? (valor as Categoria) : null);
    this.pagina.set(0);
    this.cargar();
  }

  protected irAPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.cargar();
  }

  /* ---------- Alta y edición (solo ADMIN) ---------- */

  protected nuevoProducto(): void {
    this.editando.set(null);
    this.formulario.reset({
      nombre: '',
      descripcion: '',
      precio: 0,
      categoria: 'PLA',
      pesoRollo: 1000,
    });
    this.dialogoAbierto.set(true);
  }

  protected editarProducto(producto: Producto): void {
    this.editando.set(producto);
    this.formulario.reset({
      nombre: producto.nombre,
      descripcion: producto.descripcion,
      precio: producto.precio,
      categoria: producto.categoria,
      pesoRollo: producto.pesoRollo,
    });
    this.dialogoAbierto.set(true);
  }

  protected guardar(): void {
    if (this.formulario.invalid || this.enviando()) {
      this.formulario.markAllAsTouched();
      return;
    }

    const datos = this.formulario.getRawValue();
    const producto = this.editando();
    this.enviando.set(true);

    // Mismo formulario para crear y editar: lo único que cambia es a qué
    // método del servicio se llama (POST o PUT).
    const peticion = producto
      ? this.productoService.actualizar(producto.id, datos)
      : this.productoService.crear(datos);

    peticion.subscribe({
      next: () => {
        this.avisos.exito(producto ? 'Producto actualizado.' : 'Producto creado.');
        this.dialogoAbierto.set(false);
        this.enviando.set(false);
        this.cargar();
      },
      error: (fallo: unknown) => {
        this.avisos.error(mensajeDeError(fallo));
        this.enviando.set(false);
      },
    });
  }

  /* ---------- Borrado (solo ADMIN) ---------- */

  protected confirmarBorrado(): void {
    const producto = this.aBorrar();

    if (!producto || this.enviando()) {
      return;
    }

    this.enviando.set(true);

    this.productoService.eliminar(producto.id).subscribe({
      next: () => {
        this.avisos.exito(`"${producto.nombre}" borrado del catálogo.`);
        this.aBorrar.set(null);
        this.enviando.set(false);

        // Si se ha borrado el último producto de la página, se retrocede
        // una para no quedarse en una página que ya no existe.
        if (this.productos().length === 1 && this.pagina() > 0) {
          this.pagina.update((actual) => actual - 1);
        }

        this.cargar();
      },
      error: (fallo: unknown) => {
        this.avisos.error(mensajeDeError(fallo));
        this.enviando.set(false);
      },
    });
  }
}

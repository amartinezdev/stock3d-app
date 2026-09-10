import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Dialogo } from '../../components/dialogo';
import { Icono } from '../../components/icono';
import { InventarioItem } from '../../models/inventario.model';
import { Producto } from '../../models/producto.model';
import { CategoriaPipe } from '../../pipes/categoria.pipe';
import { GramosPipe } from '../../pipes/gramos.pipe';
import { AvisosService } from '../../services/avisos.service';
import { InventarioService } from '../../services/inventario.service';
import { ProductoService } from '../../services/producto.service';
import { mensajeDeError } from '../../utils/errores';
import { normalizar } from '../../utils/texto';

/**
 * Inventario de rollos CERRADOS y las operaciones que salen de él:
 * registrar una entrada, abrir un rollo y vender.
 *
 * Después de cada operación se vuelven a pedir los datos al backend en vez
 * de "apañar" el array en memoria. Es una petición más, pero lo que se ve
 * es siempre el estado real de la base de datos: si otra pestaña, o el
 * propio servidor, cambiaran algo, aquí no se quedaría una cifra vieja.
 */
@Component({
  selector: 'app-inventario',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    Icono,
    Dialogo,
    CategoriaPipe,
    GramosPipe,
    CurrencyPipe,
  ],
  templateUrl: './inventario.html',
  styleUrl: './inventario.css',
})
export class Inventario {
  private readonly inventarioService = inject(InventarioService);
  private readonly productoService = inject(ProductoService);
  private readonly avisos = inject(AvisosService);
  private readonly fb = inject(FormBuilder);

  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly items = signal<InventarioItem[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly busqueda = signal('');

  /** Fila cuyo rollo se está abriendo, para deshabilitar solo ese botón. */
  protected readonly abriendo = signal<number | null>(null);

  /* Diálogos: cada señal es "qué diálogo está abierto y sobre qué fila". */
  protected readonly dialogoEntrada = signal(false);
  protected readonly filaVenta = signal<InventarioItem | null>(null);

  protected readonly formularioEntrada = this.fb.nonNullable.group({
    productoId: [null as number | null, Validators.required],
    cantidad: [1, [Validators.required, Validators.min(1)]],
  });

  protected readonly formularioVenta = this.fb.nonNullable.group({
    cantidad: [1, [Validators.required, Validators.min(1)]],
    precio: [0, [Validators.required, Validators.min(0.01)]],
  });

  /** Solo las filas con rollos: una fila a 0 es ruido, no información. */
  protected readonly conStock = computed(() => this.items().filter((item) => item.cantidad > 0));

  /** Búsqueda por nombre o categoría, sin distinguir mayúsculas ni tildes. */
  protected readonly filtrados = computed(() => {
    const texto = normalizar(this.busqueda());

    if (!texto) {
      return this.conStock();
    }

    return this.conStock().filter(
      (item) =>
        normalizar(item.producto.nombre).includes(texto) ||
        normalizar(item.producto.categoria).includes(texto),
    );
  });

  protected readonly totalRollos = computed(() =>
    this.conStock().reduce((total, item) => total + item.cantidad, 0),
  );

  protected readonly valorTotal = computed(() =>
    this.conStock().reduce((total, item) => total + item.cantidad * item.producto.precio, 0),
  );

  constructor() {
    this.cargar();
  }

  /* ---------- Carga ---------- */

  private cargar(): void {
    this.cargando.set(true);

    forkJoin({
      inventario: this.inventarioService.listarInventario(),
      // size grande a propósito: el desplegable de "registrar entrada"
      // necesita el catálogo entero, no una página de 10.
      catalogo: this.productoService.listar({ pagina: 0, tamano: 200 }),
    }).subscribe({
      next: ({ inventario, catalogo }) => {
        this.items.set(inventario);
        this.productos.set(catalogo.content);
        this.cargando.set(false);
      },
      error: (fallo: unknown) => {
        this.avisos.error(mensajeDeError(fallo));
        this.cargando.set(false);
      },
    });
  }

  private recargarInventario(): void {
    this.inventarioService.listarInventario().subscribe({
      next: (inventario) => this.items.set(inventario),
      error: (fallo: unknown) => this.avisos.error(mensajeDeError(fallo)),
    });
  }

  /* ---------- Entrada de stock ---------- */

  protected abrirDialogoEntrada(): void {
    this.formularioEntrada.reset({ productoId: null, cantidad: 1 });
    this.dialogoEntrada.set(true);
  }

  protected guardarEntrada(): void {
    if (this.formularioEntrada.invalid || this.enviando()) {
      this.formularioEntrada.markAllAsTouched();
      return;
    }

    const { productoId, cantidad } = this.formularioEntrada.getRawValue();
    this.enviando.set(true);

    this.inventarioService.entrada({ productoId: Number(productoId), cantidad }).subscribe({
      next: () => {
        this.avisos.exito(`Entrada registrada: +${cantidad} rollo(s).`);
        this.dialogoEntrada.set(false);
        this.enviando.set(false);
        this.recargarInventario();
      },
      error: (fallo: unknown) => {
        this.avisos.error(mensajeDeError(fallo));
        this.enviando.set(false);
      },
    });
  }

  /* ---------- Abrir rollo ---------- */

  protected abrirRollo(item: InventarioItem): void {
    if (this.abriendo() !== null) {
      return;
    }

    this.abriendo.set(item.id);

    this.inventarioService.abrirRollo({ productoId: item.producto.id }).subscribe({
      next: () => {
        this.avisos.exito(`Rollo de ${item.producto.nombre} abierto. Ya puedes consumir gramos.`);
        this.abriendo.set(null);
        this.recargarInventario();
      },
      error: (fallo: unknown) => {
        this.avisos.error(mensajeDeError(fallo));
        this.abriendo.set(null);
      },
    });
  }

  /* ---------- Venta ---------- */

  protected abrirDialogoVenta(item: InventarioItem): void {
    // El precio se rellena con el del catálogo como sugerencia, pero es
    // editable: el backend guarda el precio REAL de esta venta concreta.
    this.formularioVenta.reset({ cantidad: 1, precio: item.producto.precio });
    this.formularioVenta.controls.cantidad.setValidators([
      Validators.required,
      Validators.min(1),
      Validators.max(item.cantidad),
    ]);
    this.formularioVenta.controls.cantidad.updateValueAndValidity();
    this.filaVenta.set(item);
  }

  protected guardarVenta(): void {
    const item = this.filaVenta();

    if (!item || this.formularioVenta.invalid || this.enviando()) {
      this.formularioVenta.markAllAsTouched();
      return;
    }

    const { cantidad, precio } = this.formularioVenta.getRawValue();
    this.enviando.set(true);

    this.inventarioService
      .vender({ productoId: item.producto.id, cantidad, precio })
      .subscribe({
        next: () => {
          this.avisos.exito(`Venta registrada: ${cantidad} rollo(s) de ${item.producto.nombre}.`);
          this.filaVenta.set(null);
          this.enviando.set(false);
          this.recargarInventario();
        },
        error: (fallo: unknown) => {
          this.avisos.error(mensajeDeError(fallo));
          this.enviando.set(false);
        },
      });
  }

  protected totalVenta(): number {
    const { cantidad, precio } = this.formularioVenta.getRawValue();
    return (cantidad || 0) * (precio || 0);
  }
}

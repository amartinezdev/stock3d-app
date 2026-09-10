import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Dialogo } from '../../components/dialogo';
import { Icono } from '../../components/icono';
import { EnUsoItem } from '../../models/inventario.model';
import { CategoriaPipe } from '../../pipes/categoria.pipe';
import { GramosPipe } from '../../pipes/gramos.pipe';
import { AvisosService } from '../../services/avisos.service';
import { InventarioService } from '../../services/inventario.service';
import { mensajeDeError } from '../../utils/errores';

/** Vista de la lista: activos, agotados o todos. */
type Filtro = 'activos' | 'agotados' | 'todos';

/** Un rollo abierto con lo que hace falta para pintarlo. */
interface RolloVista extends EnUsoItem {
  porcentaje: number;
  gastados: number;
}

/** Atajos de consumo del diálogo, en gramos. */
const ATAJOS = [10, 25, 50, 100];

/**
 * Rollos ABIERTOS: los que ya están en la impresora y se van gastando a
 * gramos. Cada tarjeta enseña cuánto queda respecto al peso original del
 * rollo, que es el dato que de verdad importa para saber si llegas a
 * terminar la pieza.
 */
@Component({
  selector: 'app-en-uso',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    Icono,
    Dialogo,
    CategoriaPipe,
    GramosPipe,
    DatePipe,
    CurrencyPipe,
  ],
  templateUrl: './en-uso.html',
  styleUrl: './en-uso.css',
})
export class EnUso {
  private readonly inventarioService = inject(InventarioService);
  private readonly avisos = inject(AvisosService);
  private readonly fb = inject(FormBuilder);

  protected readonly atajos = ATAJOS;

  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly rollos = signal<EnUsoItem[]>([]);
  protected readonly filtro = signal<Filtro>('activos');
  protected readonly rolloConsumo = signal<RolloVista | null>(null);

  protected readonly formularioConsumo = this.fb.nonNullable.group({
    gramos: [10, [Validators.required, Validators.min(0.01)]],
  });

  /** Todos los rollos con el porcentaje y los gramos gastados calculados. */
  private readonly vista = computed<RolloVista[]>(() =>
    this.rollos().map((rollo) => {
      const total = rollo.producto.pesoRollo || 0;
      const restantes = rollo.gramosRestantes;

      return {
        ...rollo,
        gastados: Math.max(0, total - restantes),
        porcentaje: total === 0 ? 0 : Math.min(100, Math.round((restantes / total) * 100)),
      };
    }),
  );

  protected readonly activos = computed(() => this.vista().filter((r) => r.gramosRestantes > 0));
  protected readonly agotados = computed(() => this.vista().filter((r) => r.gramosRestantes <= 0));

  protected readonly visibles = computed(() => {
    switch (this.filtro()) {
      case 'activos':
        return this.activos();
      case 'agotados':
        return this.agotados();
      default:
        return this.vista();
    }
  });

  protected readonly gramosTotales = computed(() =>
    this.activos().reduce((total, rollo) => total + rollo.gramosRestantes, 0),
  );

  constructor() {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);

    this.inventarioService.listarEnUso().subscribe({
      next: (rollos) => {
        this.rollos.set(rollos);
        this.cargando.set(false);
      },
      error: (fallo: unknown) => {
        this.avisos.error(mensajeDeError(fallo));
        this.cargando.set(false);
      },
    });
  }

  /* ---------- Consumir gramos ---------- */

  protected abrirDialogoConsumo(rollo: RolloVista): void {
    this.formularioConsumo.reset({ gramos: Math.min(10, rollo.gramosRestantes) });
    this.formularioConsumo.controls.gramos.setValidators([
      Validators.required,
      Validators.min(0.01),
      Validators.max(rollo.gramosRestantes),
    ]);
    this.formularioConsumo.controls.gramos.updateValueAndValidity();
    this.rolloConsumo.set(rollo);
  }

  /** Botones de "+25 g", "+50 g"... del diálogo. */
  protected ponerGramos(gramos: number): void {
    const rollo = this.rolloConsumo();

    if (!rollo) {
      return;
    }

    this.formularioConsumo.patchValue({ gramos: Math.min(gramos, rollo.gramosRestantes) });
  }

  protected consumirTodo(): void {
    const rollo = this.rolloConsumo();

    if (rollo) {
      this.formularioConsumo.patchValue({ gramos: rollo.gramosRestantes });
    }
  }

  protected guardarConsumo(): void {
    const rollo = this.rolloConsumo();

    if (!rollo || this.formularioConsumo.invalid || this.enviando()) {
      this.formularioConsumo.markAllAsTouched();
      return;
    }

    const { gramos } = this.formularioConsumo.getRawValue();
    this.enviando.set(true);

    // Se manda el id del EnUso (este rollo concreto), no el del producto:
    // se pueden tener varios rollos abiertos del mismo filamento y hay que
    // decir de cuál se gasta.
    this.inventarioService.consumir({ id: rollo.id, gramos }).subscribe({
      next: () => {
        this.avisos.exito(`Consumidos ${gramos} g de ${rollo.producto.nombre}.`);
        this.rolloConsumo.set(null);
        this.enviando.set(false);
        this.cargar();
      },
      error: (fallo: unknown) => {
        this.avisos.error(mensajeDeError(fallo));
        this.enviando.set(false);
      },
    });
  }

  /** Coste aproximado de lo que queda, a precio de catálogo por rollo. */
  protected valorRestante(rollo: RolloVista): number {
    const total = rollo.producto.pesoRollo || 0;

    return total === 0 ? 0 : (rollo.gramosRestantes / total) * rollo.producto.precio;
  }
}

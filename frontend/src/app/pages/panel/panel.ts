import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Icono } from '../../components/icono';
import { GramosPipe } from '../../pipes/gramos.pipe';
import { EnUsoItem, Resumen } from '../../models/inventario.model';
import { ETIQUETAS_TIPO, Movimiento, TipoMovimiento } from '../../models/movimiento.model';
import { InventarioService } from '../../services/inventario.service';
import { MovimientoService } from '../../services/movimiento.service';
import { AvisosService } from '../../services/avisos.service';
import { mensajeDeError } from '../../utils/errores';

/** Un rollo abierto con el porcentaje que le queda ya calculado. */
interface RolloConProgreso extends EnUsoItem {
  porcentaje: number;
}

/** Una barra del gráfico: un día del histórico reciente. */
interface Barra {
  etiqueta: string;
  dia: string;
  gramos: number;
  altura: number;
}

/** Días que abarca el gráfico de consumo. */
const DIAS_GRAFICO = 14;

/**
 * Panel principal: la foto del estado del stock en una pantalla.
 *
 * Las tres peticiones que necesita (resumen, rollos abiertos y últimos
 * movimientos) se lanzan con forkJoin, que las dispara A LA VEZ y avisa
 * cuando han terminado todas. Encadenarlas sería más lento sin motivo:
 * ninguna necesita el resultado de la anterior.
 */
@Component({
  selector: 'app-panel',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Icono, GramosPipe, DecimalPipe, CurrencyPipe, DatePipe],
  templateUrl: './panel.html',
  styleUrl: './panel.css',
})
export class Panel {
  private readonly inventarioService = inject(InventarioService);
  private readonly movimientoService = inject(MovimientoService);
  private readonly avisos = inject(AvisosService);

  protected readonly cargando = signal(true);
  protected readonly resumen = signal<Resumen | null>(null);
  protected readonly enUso = signal<EnUsoItem[]>([]);
  protected readonly movimientos = signal<Movimiento[]>([]);

  /** true cuando la cuenta está recién creada y no hay nada que enseñar. */
  protected readonly sinDatos = computed(() => {
    const resumen = this.resumen();
    return (
      !!resumen &&
      resumen.rollosCerrados === 0 &&
      resumen.rollosAbiertos === 0 &&
      this.movimientos().length === 0
    );
  });

  /**
   * Rollos abiertos a los que les queda poco, para avisar antes de que se
   * acaben a mitad de una impresión. Se ordenan por porcentaje ascendente
   * (el más crítico primero) y se enseñan como mucho cuatro.
   */
  protected readonly porAgotarse = computed<RolloConProgreso[]>(() =>
    this.enUso()
      .filter((rollo) => rollo.gramosRestantes > 0)
      .map((rollo) => ({
        ...rollo,
        porcentaje: porcentajeRestante(rollo),
      }))
      .sort((a, b) => a.porcentaje - b.porcentaje)
      .slice(0, 4),
  );

  protected readonly ultimos = computed(() => this.movimientos().slice(0, 6));

  /**
   * Gramos consumidos por día en las dos últimas semanas. Se calcula en el
   * cliente a partir del histórico ya descargado: no hace falta otro
   * endpoint solo para esto, y los datos son los mismos.
   */
  protected readonly grafico = computed<Barra[]>(() => {
    const porDia = new Map<string, number>();

    for (const movimiento of this.movimientos()) {
      if (movimiento.tipo !== 'SALIDA_USO') {
        continue;
      }

      const clave = movimiento.fecha.slice(0, 10);
      porDia.set(clave, (porDia.get(clave) ?? 0) + movimiento.cantidad);
    }

    const dias: Barra[] = [];
    const hoy = new Date();

    for (let atras = DIAS_GRAFICO - 1; atras >= 0; atras--) {
      const fecha = new Date(hoy);
      fecha.setDate(hoy.getDate() - atras);

      const clave = claveIso(fecha);

      dias.push({
        dia: clave,
        etiqueta: fecha.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' }),
        gramos: porDia.get(clave) ?? 0,
        altura: 0,
      });
    }

    // La altura es relativa al día que más se consumió: así el gráfico se
    // lee igual de bien con 50 g que con 5 kg. El mínimo del 2% es para que
    // los días con consumo pequeño se sigan viendo.
    const maximo = Math.max(...dias.map((dia) => dia.gramos), 0);

    return dias.map((dia) => ({
      ...dia,
      altura: maximo === 0 ? 0 : Math.max(2, Math.round((dia.gramos / maximo) * 100)),
    }));
  });

  protected readonly consumoSemana = computed(() =>
    this.grafico()
      .slice(-7)
      .reduce((total, dia) => total + dia.gramos, 0),
  );

  constructor() {
    this.cargar();
  }

  /** "SALIDA_VENTA" -> "Venta". Las plantillas no pueden usar el mapa directamente. */
  protected etiquetaTipo(tipo: TipoMovimiento): string {
    return ETIQUETAS_TIPO[tipo];
  }

  private cargar(): void {
    this.cargando.set(true);

    forkJoin({
      resumen: this.inventarioService.resumen(),
      enUso: this.inventarioService.listarEnUso(),
      // Se piden 100 movimientos: los seis primeros son la lista de
      // "actividad reciente" y el resto alimenta el gráfico de 14 días.
      movimientos: this.movimientoService.listar({ pagina: 0, tamano: 100 }),
    }).subscribe({
      next: ({ resumen, enUso, movimientos }) => {
        this.resumen.set(resumen);
        this.enUso.set(enUso);
        this.movimientos.set(movimientos.content);
        this.cargando.set(false);
      },
      error: (fallo: unknown) => {
        this.avisos.error(mensajeDeError(fallo));
        this.cargando.set(false);
      },
    });
  }
}

function porcentajeRestante(rollo: EnUsoItem): number {
  const total = rollo.producto.pesoRollo;

  if (!total) {
    return 0;
  }

  return Math.min(100, Math.round((rollo.gramosRestantes / total) * 100));
}

/** Fecha a "AAAA-MM-DD" en hora local (toISOString daría la de Londres). */
function claveIso(fecha: Date): string {
  const mes = `${fecha.getMonth() + 1}`.padStart(2, '0');
  const dia = `${fecha.getDate()}`.padStart(2, '0');

  return `${fecha.getFullYear()}-${mes}-${dia}`;
}

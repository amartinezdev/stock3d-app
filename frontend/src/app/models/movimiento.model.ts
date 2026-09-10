import { Producto } from './producto.model';

/** Espejo del enum TipoMovimiento del backend. */
export type TipoMovimiento = 'ENTRADA' | 'SALIDA_VENTA' | 'SALIDA_USO';

/** Una fila del histórico (GET /movimientos). */
export interface Movimiento {
  id: number;
  tipo: TipoMovimiento;
  producto: Producto;
  /** Rollos en ENTRADA y SALIDA_VENTA; gramos en SALIDA_USO. */
  cantidad: number;
  /** Solo tiene valor en las ventas; en el resto llega null. */
  precio: number | null;
  /** Fecha y hora ISO ("2026-09-10T18:32:11.123"). */
  fecha: string;
  notas: string | null;
}

/** Etiqueta legible de cada tipo, para tablas y filtros. */
export const ETIQUETAS_TIPO: Record<TipoMovimiento, string> = {
  ENTRADA: 'Entrada',
  SALIDA_VENTA: 'Venta',
  SALIDA_USO: 'Consumo',
};

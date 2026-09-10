import { Producto } from './producto.model';

/** Una fila del inventario propio: rollos CERRADOS de un producto. */
export interface InventarioItem {
  id: number;
  producto: Producto;
  cantidad: number;
}

/** Un rollo ABIERTO del usuario, que se va gastando en gramos. */
export interface EnUsoItem {
  id: number;
  producto: Producto;
  gramosRestantes: number;
  /** Solo el día, en formato ISO ("2026-09-10"). */
  fechaApertura: string;
}

/** Cifras agregadas del panel principal (GET /inventario/resumen). */
export interface Resumen {
  rollosCerrados: number;
  rollosAbiertos: number;
  productosDistintos: number;
  gramosDisponibles: number;
  valorInventario: number;
  ingresosTotales: number;
  gramosConsumidos: number;
}

/* --- Cuerpos de las operaciones de stock (lo que se envía por POST) --- */

export interface EntradaPeticion {
  productoId: number;
  cantidad: number;
}

export interface AbrirRolloPeticion {
  productoId: number;
}

export interface ConsumirPeticion {
  /** Id del EnUso (el rollo abierto concreto), no el del producto. */
  id: number;
  gramos: number;
}

export interface VentaPeticion {
  productoId: number;
  cantidad: number;
  /** Precio real de esta venta; puede diferir del precio del catálogo. */
  precio: number;
}

import { Categoria } from './categoria.model';

/**
 * Un producto del catálogo, tal como lo devuelve GET /productos.
 * Los BigDecimal del backend llegan como number en el JSON.
 */
export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  pesoRollo: number;
  categoria: Categoria;
}

/**
 * Lo que se ENVÍA al crear o actualizar un producto (espejo de ProductoDTO).
 * No lleva id: al crear lo genera la base de datos y al actualizar va en la
 * URL, nunca en el cuerpo.
 */
export interface ProductoPeticion {
  nombre: string;
  descripcion: string;
  precio: number;
  categoria: Categoria;
  pesoRollo: number;
}

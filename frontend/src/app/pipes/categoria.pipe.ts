import { Pipe, PipeTransform } from '@angular/core';
import { Categoria, etiquetaCategoria } from '../models/categoria.model';

/**
 * Convierte el código del enum en algo legible: "PLA_PLUS" -> "PLA+",
 * "CARBON_FIBER" -> "Fibra de carbono". Es un pipe (y no una función suelta)
 * porque las plantillas de Angular solo pueden llamar a métodos del propio
 * componente o a pipes; así se reutiliza en todas las pantallas.
 *
 * pure: true (el valor por defecto): al depender solo de su entrada, Angular
 * lo recalcula únicamente cuando esa entrada cambia.
 */
@Pipe({ name: 'categoria' })
export class CategoriaPipe implements PipeTransform {
  transform(valor: Categoria): string {
    return etiquetaCategoria(valor);
  }
}

import { formatNumber } from '@angular/common';
import { inject, LOCALE_ID, Pipe, PipeTransform } from '@angular/core';

/**
 * Enseña gramos de la forma en que los diría una persona: 850 g tal cual,
 * pero 1750 g como "1,75 kg". Los números van con el formato del idioma
 * (coma decimal en español), reutilizando formatNumber de Angular en vez de
 * apañarlo con toFixed.
 */
@Pipe({ name: 'gramos' })
export class GramosPipe implements PipeTransform {
  private readonly idioma = inject(LOCALE_ID);

  transform(valor: number | null | undefined): string {
    const gramos = valor ?? 0;

    if (Math.abs(gramos) >= 1000) {
      return `${formatNumber(gramos / 1000, this.idioma, '1.0-2')} kg`;
    }

    return `${formatNumber(gramos, this.idioma, '1.0-1')} g`;
  }
}

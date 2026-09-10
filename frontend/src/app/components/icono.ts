import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Pinta uno de los iconos del sprite que hay al final de index.html.
 * Uso: <app-icono nombre="caja" /> o <app-icono nombre="caja" tamano="sm" />.
 *
 * El SVG hereda el color del texto (stroke: currentColor en styles.css), así
 * que el icono cambia de color solo al cambiar el color del botón o del
 * texto que lo rodea. aria-hidden porque los iconos aquí son decorativos:
 * siempre van acompañados de texto, o el botón lleva su propio aria-label.
 */
@Component({
  selector: 'app-icono',
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: ':host { display: contents; }',
  template: `
    <svg
      class="icono"
      [class.icono-sm]="tamano() === 'sm'"
      [class.icono-lg]="tamano() === 'lg'"
      [class.icono-xl]="tamano() === 'xl'"
      [class.girando]="girando()"
      aria-hidden="true"
    >
      <use [attr.href]="'#i-' + nombre()"></use>
    </svg>
  `,
})
export class Icono {
  /** Nombre del símbolo, sin el prefijo "i-" (por ejemplo "caja"). */
  readonly nombre = input.required<string>();

  readonly tamano = input<'sm' | 'md' | 'lg' | 'xl'>('md');

  /** Lo hace girar sobre sí mismo; se usa como indicador de "cargando". */
  readonly girando = input(false);
}

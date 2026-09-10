import { Service, signal } from '@angular/core';

export type TipoAviso = 'exito' | 'error' | 'info';

export interface Aviso {
  id: number;
  tipo: TipoAviso;
  texto: string;
}

/** Milisegundos que se queda un aviso en pantalla antes de irse solo. */
const DURACION = 4500;

/**
 * Los mensajitos flotantes de la esquina ("Producto creado", "No puedes
 * vender más de lo que tienes"). Cualquier componente puede pedir uno; el
 * componente Avisos es el único que los pinta, leyendo esta misma señal.
 */
@Service()
export class AvisosService {
  private readonly _avisos = signal<Aviso[]>([]);
  private siguienteId = 0;

  readonly avisos = this._avisos.asReadonly();

  exito(texto: string): void {
    this.mostrar('exito', texto);
  }

  error(texto: string): void {
    this.mostrar('error', texto);
  }

  info(texto: string): void {
    this.mostrar('info', texto);
  }

  cerrar(id: number): void {
    this._avisos.update((lista) => lista.filter((aviso) => aviso.id !== id));
  }

  private mostrar(tipo: TipoAviso, texto: string): void {
    const id = this.siguienteId++;

    this._avisos.update((lista) => [...lista, { id, tipo, texto }]);

    // Se cierra solo pasado un rato; si la persona lo cierra antes a mano,
    // este filter simplemente no encuentra nada que quitar.
    setTimeout(() => this.cerrar(id), DURACION);
  }
}

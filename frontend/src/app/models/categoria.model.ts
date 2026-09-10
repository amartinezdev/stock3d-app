/**
 * Espejo en TypeScript del enum Categoria del backend. Es un tipo unión de
 * literales en vez de un enum de TS: el JSON que llega es texto plano
 * ("PLA"), así que este tipo describe exactamente lo que puede venir y el
 * compilador avisa si en algún sitio se escribe una categoría inventada.
 */
export type Categoria =
  | 'PLA'
  | 'PLA_PLUS'
  | 'PETG'
  | 'ABS'
  | 'ASA'
  | 'TPU'
  | 'NYLON'
  | 'PC'
  | 'HIPS'
  | 'PVA'
  | 'BVOH'
  | 'PP'
  | 'PEEK'
  | 'PEI'
  | 'PMMA'
  | 'WOOD'
  | 'CARBON_FIBER'
  | 'GLASS_FIBER'
  | 'SILK'
  | 'MARBLE'
  | 'METAL'
  | 'GLOW_IN_THE_DARK'
  | 'CONDUCTIVE';

/** Todas las categorías, en el mismo orden que el enum del backend. */
export const CATEGORIAS: readonly Categoria[] = [
  'PLA',
  'PLA_PLUS',
  'PETG',
  'ABS',
  'ASA',
  'TPU',
  'NYLON',
  'PC',
  'HIPS',
  'PVA',
  'BVOH',
  'PP',
  'PEEK',
  'PEI',
  'PMMA',
  'WOOD',
  'CARBON_FIBER',
  'GLASS_FIBER',
  'SILK',
  'MARBLE',
  'METAL',
  'GLOW_IN_THE_DARK',
  'CONDUCTIVE',
];

/**
 * Nombre bonito para pantalla. Solo se listan las que no se leen bien tal
 * cual; para el resto vale el propio código ("PLA", "PETG"...).
 */
const ETIQUETAS: Partial<Record<Categoria, string>> = {
  PLA_PLUS: 'PLA+',
  CARBON_FIBER: 'Fibra de carbono',
  GLASS_FIBER: 'Fibra de vidrio',
  GLOW_IN_THE_DARK: 'Glow in the dark',
  WOOD: 'Madera',
  MARBLE: 'Mármol',
  METAL: 'Metal',
  SILK: 'Silk',
  CONDUCTIVE: 'Conductivo',
};

export function etiquetaCategoria(categoria: Categoria): string {
  return ETIQUETAS[categoria] ?? categoria;
}

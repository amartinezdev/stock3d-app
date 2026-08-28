export interface ProductoModel {

  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  pesoRollo: number;

  categoria: 'PLA' |
  'PLA_PLUS' |
  'PETG' |
  'ABS' |
  'ASA' |
  'TPU' |
  'NYLON' |
  'PC' |
  'HIPS' |
  'PVA' |
  'BVOH' |
  'PP' |
  'PEEK' |
  'PEI' |
  'PMMA' |
  'WOOD' |
  'CARBON_FIBER' |
  'GLASS_FIBER' |
  'SILK' |
  'MARBLE' |
  'METAL' |
  'GLOW_IN_THE_DARK' |
  'CONDUCTIVE'



}

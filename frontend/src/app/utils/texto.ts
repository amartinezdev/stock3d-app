/**
 * Prepara un texto para buscar: minúsculas y sin tildes, para que "petg"
 * encuentre "PETG" y "marmol" encuentre "Mármol".
 *
 * normalize('NFD') separa cada letra acentuada en dos caracteres (letra +
 * acento suelto) y el replace borra esos acentos: \p{Mn} es la categoría
 * Unicode "marca sin espaciado", justo lo que son las tildes ya separadas.
 * La bandera u es obligatoria para poder usar \p{...}.
 */
export function normalizar(texto: string): string {
  return texto.trim().toLowerCase().normalize('NFD').replace(/\p{Mn}/gu, '');
}

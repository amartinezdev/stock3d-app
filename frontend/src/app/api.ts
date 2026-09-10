/**
 * Prefijo de todas las llamadas a la API.
 *
 * Es una ruta RELATIVA a propósito, no "http://localhost:8080". Así el
 * mismo código compilado vale en el portátil y en el servidor, y las
 * peticiones salen siempre al mismo origen que la página:
 *
 * - En desarrollo, el servidor de Angular hace de proxy y reenvía todo lo
 *   que empiece por /api al backend del 8080 (ver proxy.conf.json).
 * - En producción, el proxy inverso (Caddy) hace exactamente lo mismo.
 *
 * Efecto secundario muy útil: al ser el mismo origen, el navegador no
 * aplica CORS. Se acabaron los "no se puede conectar con el servidor" por
 * un puerto distinto al esperado.
 */
export const API_URL = '/api';

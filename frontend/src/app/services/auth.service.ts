import { HttpClient } from '@angular/common/http';
import { computed, inject, Service, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, switchMap, tap } from 'rxjs';
import { API_URL } from '../api';
import {
  LoginPeticion,
  RegistroPeticion,
  TokenRespuesta,
  Usuario,
} from '../models/usuario.model';

/** Clave del localStorage donde vive el JWT entre recargas. */
const CLAVE_TOKEN = 'stock3d.token';

/**
 * Estado de sesión de toda la app: el token y el usuario actual.
 *
 * El token se guarda en localStorage porque es lo que permite seguir dentro
 * al recargar la página (en memoria se perdería). El precio a pagar es que
 * cualquier script que se colara en la web podría leerlo (XSS) - la
 * alternativa "seria" es una cookie httpOnly, que el JS no puede leer, pero
 * eso exige que el backend la emita y gestione CSRF.
 *
 * El rol NO se guarda en localStorage: se pide a GET /me cada vez que
 * arranca la app. Si se guardara, cualquiera podría editarlo a mano en el
 * navegador y "ascenderse" a ADMIN en la interfaz. Ojo: eso solo cambiaría
 * lo que se ve, porque el backend vuelve a comprobar el rol en cada
 * petición - el frontend nunca es la barrera de seguridad real.
 */
@Service()
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly _token = signal<string | null>(leerTokenGuardado());
  private readonly _usuario = signal<Usuario | null>(null);

  /** El JWT actual, o null si no hay sesión. */
  readonly token = this._token.asReadonly();

  /** Datos del usuario logueado; null hasta que responde GET /me. */
  readonly usuario = this._usuario.asReadonly();

  /** Hay token guardado (aunque aún no se sepa de quién es). */
  readonly haySesion = computed(() => this._token() !== null);

  readonly esAdmin = computed(() => this._usuario()?.rol === 'ADMIN');

  /** Iniciales para el avatar ("Álvaro Martínez" -> "AM"). */
  readonly iniciales = computed(() => {
    const nombre = this._usuario()?.nombre ?? '';
    const partes = nombre.trim().split(/\s+/).filter(Boolean);

    if (partes.length === 0) {
      return '?';
    }

    const primera = partes[0]!.charAt(0);
    const segunda = partes.length > 1 ? partes[partes.length - 1]!.charAt(0) : '';

    return (primera + segunda).toUpperCase();
  });

  /**
   * Login: pide el token, lo guarda y encadena con /me para saber ya quién
   * ha entrado. switchMap y no dos subscribes anidados: la segunda petición
   * depende de la primera y así el componente recibe un único Observable
   * que termina cuando todo está listo.
   */
  login(credenciales: LoginPeticion): Observable<Usuario> {
    return this.http.post<TokenRespuesta>(`${API_URL}/login`, credenciales).pipe(
      tap((respuesta) => this.guardarToken(respuesta.token)),
      switchMap(() => this.cargarPerfil()),
    );
  }

  /** Registro. No devuelve token: al terminar hay que iniciar sesión. */
  registrar(datos: RegistroPeticion): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_URL}/registrar`, datos);
  }

  /** Pregunta al backend quién es el dueño del token guardado. */
  cargarPerfil(): Observable<Usuario> {
    return this.http
      .get<Usuario>(`${API_URL}/me`)
      .pipe(tap((usuario) => this._usuario.set(usuario)));
  }

  /** Cierre de sesión pedido por la persona: limpia y vuelve al login. */
  cerrarSesion(): void {
    this.limpiar();
    void this.router.navigate(['/login']);
  }

  /**
   * Cierre de sesión forzado (lo llama el interceptor cuando el backend
   * responde 401: token caducado o inválido). Guarda en la URL a dónde
   * quería ir la persona, para devolverla ahí después de volver a entrar.
   */
  expulsarPorTokenInvalido(): void {
    const destino = this.router.url;
    this.limpiar();
    void this.router.navigate(['/login'], {
      queryParams: destino && destino !== '/login' ? { volverA: destino } : {},
    });
  }

  private guardarToken(token: string): void {
    this._token.set(token);

    try {
      localStorage.setItem(CLAVE_TOKEN, token);
    } catch {
      // Navegación privada o almacenamiento bloqueado: la sesión seguirá
      // funcionando en memoria hasta que se recargue la página.
    }
  }

  private limpiar(): void {
    this._token.set(null);
    this._usuario.set(null);

    try {
      localStorage.removeItem(CLAVE_TOKEN);
    } catch {
      // Nada que limpiar si no se pudo guardar.
    }
  }
}

function leerTokenGuardado(): string | null {
  try {
    return localStorage.getItem(CLAVE_TOKEN);
  } catch {
    return null;
  }
}

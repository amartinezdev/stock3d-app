import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Icono } from '../../components/icono';
import { AuthService } from '../../services/auth.service';
import { AvisosService } from '../../services/avisos.service';
import { mensajeDeError } from '../../utils/errores';

/**
 * Pantalla de inicio de sesión. El formulario es reactivo (el estado vive
 * en la clase, no en el HTML), que es lo que permite validar, deshabilitar
 * el botón y leer los valores desde TypeScript con seguridad de tipos.
 *
 * La validación de aquí es solo comodidad para la persona: quien decide de
 * verdad si las credenciales valen es el backend. El frontend nunca es la
 * barrera de seguridad.
 */
@Component({
  selector: 'app-login',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, Icono],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly ruta = inject(ActivatedRoute);
  private readonly avisos = inject(AvisosService);

  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly formulario = this.fb.nonNullable.group({
    userName: ['', Validators.required],
    password: ['', Validators.required],
  });

  protected enviar(): void {
    if (this.formulario.invalid || this.cargando()) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.auth.login(this.formulario.getRawValue()).subscribe({
      next: (usuario) => {
        this.avisos.exito(`Hola de nuevo, ${usuario.nombre}.`);

        // volverA lo pone el guard cuando alguien intenta entrar en una
        // pantalla privada sin sesión: así, tras el login, se le devuelve
        // justo a donde iba en vez de soltarle siempre en el panel.
        const destino = this.ruta.snapshot.queryParamMap.get('volverA') ?? '/panel';
        void this.router.navigateByUrl(destino);
      },
      error: (fallo: unknown) => {
        this.error.set(mensajeDeError(fallo));
        this.cargando.set(false);
      },
    });
  }
}

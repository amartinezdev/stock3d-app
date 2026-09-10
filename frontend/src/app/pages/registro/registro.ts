import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { Icono } from '../../components/icono';
import { AuthService } from '../../services/auth.service';
import { AvisosService } from '../../services/avisos.service';
import { mensajeDeError } from '../../utils/errores';

/**
 * Validador de grupo: las dos contraseñas tienen que coincidir. Va en el
 * FormGroup y no en un control suelto porque necesita ver dos campos a la
 * vez; un validador de control solo conoce su propio valor.
 */
function contrasenasIguales(grupo: AbstractControl): ValidationErrors | null {
  const password = grupo.get('password')?.value;
  const repetir = grupo.get('repetir')?.value;

  return password && repetir && password !== repetir ? { noCoinciden: true } : null;
}

/**
 * Alta de cuenta. El rol no se pide ni se manda: lo fija el backend como
 * USER, para que nadie pueda auto-asignarse ADMIN desde el navegador.
 *
 * Al terminar el registro se encadena un login con esas mismas credenciales
 * (switchMap), para no obligar a escribirlas otra vez.
 */
@Component({
  selector: 'app-registro',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, Icono],
  templateUrl: './registro.html',
  styleUrl: '../login/login.css',
})
export class Registro {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly avisos = inject(AvisosService);

  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly formulario = this.fb.nonNullable.group(
    {
      nombre: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      userName: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      repetir: ['', Validators.required],
    },
    { validators: contrasenasIguales },
  );

  protected enviar(): void {
    if (this.formulario.invalid || this.cargando()) {
      this.formulario.markAllAsTouched();
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    const { nombre, email, userName, password } = this.formulario.getRawValue();

    this.auth
      .registrar({ nombre, email, userName, password })
      .pipe(switchMap(() => this.auth.login({ userName, password })))
      .subscribe({
        next: (usuario) => {
          this.avisos.exito(`Cuenta creada. Bienvenido, ${usuario.nombre}.`);
          void this.router.navigate(['/panel']);
        },
        error: (fallo: unknown) => {
          this.error.set(mensajeDeError(fallo));
          this.cargando.set(false);
        },
      });
  }
}

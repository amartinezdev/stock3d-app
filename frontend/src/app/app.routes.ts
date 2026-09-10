import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { invitadoGuard } from './guards/invitado.guard';

/**
 * Mapa de la aplicación.
 *
 * Todas las pantallas se cargan con loadComponent (lazy loading): Angular
 * genera un fichero JS por pantalla y solo lo descarga la primera vez que
 * se entra en ella. Así quien abre el login no se traga de golpe el código
 * de las tablas, los diálogos y el panel.
 *
 * Las rutas privadas cuelgan de una ruta padre sin path que monta el
 * Layout: es lo que hace que la barra lateral y la cabecera se pinten una
 * sola vez y solo cambie el contenido del <router-outlet> interno. El
 * canActivate del padre protege de una vez a todos sus hijos.
 */
export const routes: Routes = [
  {
    path: 'login',
    title: 'Entrar · Stock3D',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },
  {
    path: 'registro',
    title: 'Crear cuenta · Stock3D',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./pages/registro/registro').then((m) => m.Registro),
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/layout').then((m) => m.Layout),
    children: [
      {
        path: 'panel',
        title: 'Panel · Stock3D',
        loadComponent: () => import('./pages/panel/panel').then((m) => m.Panel),
      },
      {
        path: 'inventario',
        title: 'Inventario · Stock3D',
        loadComponent: () => import('./pages/inventario/inventario').then((m) => m.Inventario),
      },
      {
        path: 'en-uso',
        title: 'Material en uso · Stock3D',
        loadComponent: () => import('./pages/en-uso/en-uso').then((m) => m.EnUso),
      },
      {
        path: 'productos',
        title: 'Catálogo · Stock3D',
        loadComponent: () => import('./pages/productos/productos').then((m) => m.Productos),
      },
      {
        path: 'movimientos',
        title: 'Movimientos · Stock3D',
        loadComponent: () =>
          import('./pages/movimientos/movimientos').then((m) => m.Movimientos),
      },
      // pathMatch 'full' es obligatorio aquí: sin él, la ruta vacía haría
      // match con CUALQUIER URL (todas empiezan por "") y nunca se llegaría
      // a las de arriba.
      { path: '', pathMatch: 'full', redirectTo: 'panel' },
      {
        path: '**',
        title: 'Página no encontrada · Stock3D',
        loadComponent: () =>
          import('./pages/no-encontrado/no-encontrado').then((m) => m.NoEncontrado),
      },
    ],
  },
];

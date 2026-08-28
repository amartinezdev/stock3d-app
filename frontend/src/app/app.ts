import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { OnInit, inject } from '@angular/core';
import { ProductoService } from './services/producto.service';
import { ProductoModel } from './models/producto.model';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {

  protected readonly title = signal('frontend');
  protected readonly contador = signal(0);
  protected readonly productoService = inject(ProductoService);
  protected readonly productos = signal<ProductoModel[]>([]);

  ngOnInit(): void {
    this.productoService.listarProductos().subscribe(
      { next: (pagina) => this.productos.set(pagina.content) });
  }
}

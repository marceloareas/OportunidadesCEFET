import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';


@Component({
  selector: 'app-navbar-left',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './navbar-left.html',
  styleUrl: './navbar-left.css'
})
export class NavbarLeft {
  usuarioId: string | null = null;

  ngOnInit(): void {
    if (typeof window === 'undefined') return;

    const usuarioSalvo = localStorage.getItem('usuario');
    if (usuarioSalvo) {
      try {
        const usuario = JSON.parse(usuarioSalvo);
        this.usuarioId = usuario.id?.toString() || null;
      } catch (error) {
        console.error('Erro ao ler dados do usuário:', error);
      }
    }
  }
}

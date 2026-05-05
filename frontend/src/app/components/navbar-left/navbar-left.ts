import { Component } from '@angular/core';
import { Router } from '@angular/router';


@Component({
  selector: 'app-navbar-left',
  imports: [],
  templateUrl: './navbar-left.html',
  styleUrl: './navbar-left.css'
})
export class NavbarLeft {
  usuarioId: string | null = null;

  constructor(private router: Router) {}

  ngOnInit(): void {
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

  goPerfil(): void {
    if (!this.usuarioId) {
      return;
    }

    this.router.navigate(['/perfil', this.usuarioId]);
  }

  setView(view: string) {
    this.router.navigate([`/${view}`]);
  }  
}

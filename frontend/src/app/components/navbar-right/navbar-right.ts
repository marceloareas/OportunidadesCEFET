import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar-right',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar-right.html',
  styleUrl: './navbar-right.css'
})
export class NavbarRight implements OnInit {
  usuarioNome: string = 'Usuário';
  usuarioFuncao: string = 'Função não definida';

  constructor(private router: Router) {}

  ngOnInit(): void {
    // 🔹 Recupera o usuário salvo no login
    const usuarioSalvo = localStorage.getItem('usuario');
    if (usuarioSalvo) {
      try {
        const usuario = JSON.parse(usuarioSalvo);
        this.usuarioNome = usuario.nome || 'Usuário';
        this.usuarioFuncao = usuario.funcao || 'Função não definida';
      } catch (error) {
        console.error('Erro ao ler dados do usuário:', error);
      }
    }
  }

  // 🔹 Função de logout
  logout(): void {
    localStorage.clear();
    this.router.navigate(['/login']);
  }
}

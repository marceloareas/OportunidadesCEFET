import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { avatarOuPadrao } from '../../utils/avatar';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar-right',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar-right.html',
  styleUrl: './navbar-right.css'
})
export class NavbarRight implements OnInit {
  usuarioId: string | null = null;
  usuarioNome: string = 'Usuário';
  usuarioFuncao: string = 'Função não definida';
  usuarioMatricula: string = 'Matrícula';
  usuarioImagem: string | null = null;
  readonly avatarOuPadrao = avatarOuPadrao;
  private authService = inject(AuthService);

  constructor(private router: Router) {}

  ngOnInit(): void {
    if (typeof window === 'undefined') {
      return;
    }

    const usuarioSalvo = localStorage.getItem('usuario');
    if (usuarioSalvo) {
      try {
        const usuario = JSON.parse(usuarioSalvo);
        this.usuarioId = usuario.id?.toString() || null;
        this.usuarioNome = usuario.nome || 'Usuário';
        this.usuarioFuncao = usuario.funcao || 'Função não definida';
        this.usuarioMatricula = usuario.matricula || '';
        this.usuarioImagem = usuario.imagemPerfil || null;
      } catch (error) {
        console.error('Erro ao ler dados do usuário:', error);
      }
    }
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

  goConfig(): void {
    this.router.navigate(['/config']);
  }

  // 🔹 Função de logout
  logout(): void {
    this.authService.encerrarSessao();
    this.router.navigate(['/login']);
  }
}

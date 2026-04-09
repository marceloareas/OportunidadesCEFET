import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { UsuarioService, Usuario } from '../../services/usuario.service';

@Component({
  selector: 'app-editar-perfil',
  imports: [CommonModule, FormsModule, NavbarTop, NavbarLeft],
  templateUrl: './editar-perfil.html',
  styleUrl: './editar-perfil.css',
})
export class EditarPerfil {
  nome = signal<string>('');
  email = signal<string>('');
  senha = signal<string>('');
  confirmaSenha = signal<string>('');
  userId?: string;
  funcao?: string;
  matricula?: string;

  constructor(private usuarioService: UsuarioService, private router: Router) {
    const stored = localStorage.getItem('usuario');
    if (stored) {
      try {
        const u = JSON.parse(stored) as any;
        this.userId = u.id?.toString();
        this.nome.set(u.nome || '');
        this.email.set(u.email || '');
        this.funcao = u.funcao;
        this.matricula = u.matricula;
      } catch (e) {
        console.error('Erro ao parsear usuário do localStorage', e);
      }
    }
  }

  onSubmit(event: Event) {
    event.preventDefault();

    if (this.senha() && this.senha() !== this.confirmaSenha()) {
      alert('As senhas não coincidem.');
      return;
    }

    if (!this.userId) {
      alert('Usuário não encontrado. Faça login novamente.');
      return;
    }

    const payload: any = {
      nome: this.nome(),
      email: this.email(),
    };

    // Mantém funcao e matricula para não "zerar" no backend
    if (this.funcao) {
      payload.funcao = this.funcao;
    }
    if (this.matricula) {
      payload.matricula = this.matricula;
    }

    if (this.senha()) {
      payload.senha = this.senha();
    }

    this.usuarioService.atualizar(this.userId, payload).subscribe({
      next: (updated) => {
        alert('Perfil atualizado com sucesso.');
        // Atualiza localStorage com novos dados (mantém id)
        const funcaoAtualizada = updated.funcao || this.funcao;
        const usuarioNormalizado = {
          id: updated.id?.toString() || this.userId,
          nome: updated.nome,
          email: updated.email,
          funcao: funcaoAtualizada,
          matricula: updated.matricula || this.matricula,
        };
        localStorage.setItem('usuario', JSON.stringify(usuarioNormalizado));
        if (funcaoAtualizada) {
          const tipo =
            funcaoAtualizada.toLowerCase() === 'professor' ? 'professor' : 'aluno';
          localStorage.setItem('tipoUsuario', tipo);
        }
        this.router.navigate(['/home']);
      },
      error: (err) => {
        console.error('Erro ao atualizar usuário:', err);
        alert('Erro ao atualizar perfil. Tente novamente.');
      },
    });
  }

  onCancel() {
    this.router.navigate(['/home']);
  }
}

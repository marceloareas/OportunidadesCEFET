import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { Usuario, UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarTop, NavbarLeft, NavbarRight],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css'
})
export class Perfil {
  usuarioPerfil = signal<Usuario | null>(null);
  usuarioLogadoId = signal<string | null>(null);
  carregando = signal<boolean>(true);
  salvando = signal<boolean>(false);
  erro = signal<string>('');

  linkPortfolio = signal<string>('');
  linkCurriculo = signal<string>('');

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private usuarioService: UsuarioService
  ) {}

  ngOnInit() {
    if (typeof window === 'undefined') {
      return;
    }

    const usuarioSalvo = localStorage.getItem('usuario');
    if (usuarioSalvo) {
      try {
        const parsed = JSON.parse(usuarioSalvo);
        this.usuarioLogadoId.set(parsed?.id || null);
      } catch {
        this.usuarioLogadoId.set(null);
      }
    }

    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (!id) {
        this.erro.set('Perfil inválido.');
        this.carregando.set(false);
        return;
      }
      this.buscarPerfil(id);
    });
  }

  private buscarPerfil(id: string) {
    this.carregando.set(true);
    this.erro.set('');

    this.usuarioService.buscarPorId(id).subscribe({
      next: (usuario) => {
        this.usuarioPerfil.set(usuario);
        this.linkPortfolio.set(usuario.linkPortfolio || '');
        this.linkCurriculo.set(usuario.linkCurriculo || '');
        this.carregando.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar perfil:', err);
        this.erro.set('Não foi possível carregar este perfil.');
        this.carregando.set(false);
      }
    });
  }

  isProprioPerfil(): boolean {
    return !!this.usuarioPerfil()?.id && this.usuarioPerfil()?.id === this.usuarioLogadoId();
  }

  getTipoUsuarioLabel(funcao?: string): string {
    if (!funcao) return 'Usuário';
    const normalizado = funcao.toLowerCase();
    if (normalizado === 'professor') return 'Professor';
    if (normalizado === 'aluno') return 'Aluno';
    return funcao;
  }

  private validarLinkOpcional(valor: string): boolean {
    if (!valor.trim()) return true;
    return /^https?:\/\//i.test(valor.trim());
  }

  salvarLinks() {
    const perfil = this.usuarioPerfil();
    if (!perfil?.id || !this.isProprioPerfil()) {
      return;
    }

    if (!this.validarLinkOpcional(this.linkPortfolio())) {
      alert('O link do portfólio deve começar com http:// ou https://');
      return;
    }

    if (!this.validarLinkOpcional(this.linkCurriculo())) {
      alert('O link do currículo deve começar com http:// ou https://');
      return;
    }

    const payload: Usuario = {
      nome: perfil.nome,
      email: perfil.email,
      matricula: perfil.matricula,
      funcao: perfil.funcao,
      imagemPerfil: perfil.imagemPerfil,
      linkPortfolio: this.linkPortfolio().trim(),
      linkCurriculo: this.linkCurriculo().trim()
    };

    this.salvando.set(true);

    this.usuarioService.atualizar(perfil.id, payload).subscribe({
      next: (updated) => {
        this.usuarioPerfil.set(updated);
        this.linkPortfolio.set(updated.linkPortfolio || '');
        this.linkCurriculo.set(updated.linkCurriculo || '');

        const usuarioSalvo = localStorage.getItem('usuario');
        if (usuarioSalvo) {
          try {
            const parsed = JSON.parse(usuarioSalvo);
            if (parsed?.id === updated.id) {
              const normalizado = {
                ...parsed,
                nome: updated.nome,
                email: updated.email,
                funcao: updated.funcao,
                matricula: updated.matricula,
                imagemPerfil: updated.imagemPerfil,
                linkPortfolio: updated.linkPortfolio,
                linkCurriculo: updated.linkCurriculo
              };
              localStorage.setItem('usuario', JSON.stringify(normalizado));
            }
          } catch {
            // Ignora erro de parse no cache local.
          }
        }

        this.salvando.set(false);
        alert('Links do perfil atualizados com sucesso.');
      },
      error: (err) => {
        console.error('Erro ao salvar links do perfil:', err);
        this.salvando.set(false);
        alert('Não foi possível salvar os links do perfil.');
      }
    });
  }

  voltarInicio() {
    this.router.navigate(['/home']);
  }
}

import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { Usuario, UsuarioService } from '../../services/usuario.service';

import {
  DEFAULT_PROFILE_IMAGE_CROP,
  ProfileImageCropPosition,
  cropProfileImageToSquare
} from '../../utils/profile-image';

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
  imagemSelecionada: File | null = null;
  imagemPreview: string | null = null;
  imagemErro: string | null = null;
  imagemPerfilBase64: string | null = null;

  imagemCrop: ProfileImageCropPosition = { ...DEFAULT_PROFILE_IMAGE_CROP };

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

  async onImageSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files[0]) {
      const file = input.files[0];

      if (!file.type.startsWith('image/')) {
        this.imagemErro = 'Selecione um arquivo de imagem válido.';
        this.imagemSelecionada = null;
        this.imagemPerfilBase64 = null;
        input.value = '';
        return;
      }

      this.imagemSelecionada = file;
      this.imagemCrop = { ...DEFAULT_PROFILE_IMAGE_CROP };

      await this.processarImagemPerfilSelecionada();
    }
  }

  async atualizarCorteImagemPerfil(axis: 'x' | 'y', event: Event): Promise<void> {
    const value = Number((event.target as HTMLInputElement).value);

    this.imagemCrop = {
      ...this.imagemCrop,
      [axis]: value
    };

    await this.processarImagemPerfilSelecionada();
  }

  private async processarImagemPerfilSelecionada(): Promise<void> {
    if (!this.imagemSelecionada) return;

    try {
      this.imagemPerfilBase64 = await cropProfileImageToSquare(
        this.imagemSelecionada,
        this.imagemCrop
      );

      this.imagemPreview = this.imagemPerfilBase64;
      this.imagemErro = '';
    } catch (error) {
      console.error('Erro ao processar imagem de perfil:', error);
      this.imagemErro = 'Não foi possível processar a imagem.';
      this.imagemPerfilBase64 = null;
    }
  }

  salvarImagemPerfil(): void {
    if (!this.imagemPerfilBase64) {
      this.imagemErro = 'Selecione uma imagem antes de salvar.';
      return;
    }

    const perfil = this.usuarioPerfil();
    if (!perfil?.id) {
      this.imagemErro = 'Usuário não encontrado.';
      return;
    }

    const payload: Usuario = {
      nome: perfil.nome,
      email: perfil.email,
      matricula: perfil.matricula,
      funcao: perfil.funcao,
      linkPortfolio: perfil.linkPortfolio,
      linkCurriculo: perfil.linkCurriculo,
      imagemPerfil: this.imagemPerfilBase64
    };

    this.usuarioService.atualizar(perfil.id.toString(), payload).subscribe({
      next: (usuarioAtualizado) => {
        this.usuarioPerfil.update((atual) =>
          atual
            ? {
                ...atual,
                imagemPerfil: usuarioAtualizado.imagemPerfil || this.imagemPreview || undefined
              }
            : atual
        );

        const usuarioSalvo = localStorage.getItem('usuario');
        if (usuarioSalvo) {
          const usuarioParseado = JSON.parse(usuarioSalvo);
          usuarioParseado.imagemPerfil =
            usuarioAtualizado.imagemPerfil || this.imagemPreview;

          localStorage.setItem('usuario', JSON.stringify(usuarioParseado));
        }

        this.fecharModalImagem();
      },
      error: (err) => {
        console.error('Erro ao salvar imagem de perfil:', err);
        this.imagemErro = 'Erro ao salvar imagem. Tente novamente.';
      }
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

  modalImagemAberto = signal(false);

  abrirModalImagem(): void {
    this.modalImagemAberto.set(true);
  }

  fecharModalImagem(): void {
    this.modalImagemAberto.set(false);
    this.imagemSelecionada = null;
    this.imagemErro = null;
    this.imagemPerfilBase64 = null;
    this.imagemCrop = { ...DEFAULT_PROFILE_IMAGE_CROP };
    this.imagemPreview = null;
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

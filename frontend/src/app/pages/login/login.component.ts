import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UsuarioService, Usuario } from '../../services/usuario.service';
import {
  DEFAULT_PROFILE_IMAGE_CROP,
  ProfileImageCropPosition,
  cropProfileImageToSquare
} from '../../utils/profile-image';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class Login {
  isRegistering = signal<boolean>(false);

  email = signal<string>('');
  senha = signal<string>('');
  nome = signal<string>('');
  matricula = signal<string>('');
  confirmaSenha = signal<string>('');
  selecionada = signal<string>(''); // "Aluno" ou "Professor"
  opcoes = ['Aluno', 'Professor'];

  imagemPerfilBase64: string | null = null;
  imagemPreview: string | null = null;
  imagemErro: string = '';
  imagemSelecionada: File | null = null;
  imagemCrop: ProfileImageCropPosition = { ...DEFAULT_PROFILE_IMAGE_CROP };

  constructor(
    private router: Router,
    private usuarioService: UsuarioService,
    private authService: AuthService
  ) {}

  async onImageSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      if (!file.type.startsWith('image/')) {
        this.imagemErro = 'Selecione um arquivo de imagem válido.';
        this.imagemSelecionada = null;
        this.imagemPerfilBase64 = null;
        this.imagemPreview = null;
        input.value = '';
        return;
      }

      this.imagemSelecionada = file;
      this.imagemCrop = { ...DEFAULT_PROFILE_IMAGE_CROP };
      await this.processarImagemPerfilSelecionada();
    } else {
      this.imagemSelecionada = null;
      this.imagemPerfilBase64 = null;
      this.imagemPreview = null;
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
      this.imagemPerfilBase64 = await cropProfileImageToSquare(this.imagemSelecionada, this.imagemCrop);
      this.imagemPreview = this.imagemPerfilBase64;
      this.imagemErro = '';
    } catch (error) {
      console.error('Erro ao processar imagem de perfil:', error);
      this.imagemErro = 'Não foi possível processar a imagem.';
      this.imagemPerfilBase64 = null;
      this.imagemPreview = null;
    }
  }

  triggerFileInput(): void {
    const fileInput = document.getElementById('imagemPerfil') as HTMLInputElement;
    if (fileInput) {
      fileInput.click();
    }
  }

  toggleForm(): void {
    this.isRegistering.set(!this.isRegistering());
    const tab = document.querySelector('.tab') as HTMLElement;
    if (tab) tab.classList.toggle('move-left');
  }

  onLoginSubmit(event: Event): void {
    event.preventDefault();

    if (!this.email() || !this.senha()) {
      alert('Preencha e-mail e senha.');
      return;
    }

    this.authService.login({ email: this.email(), senha: this.senha() }).subscribe({
      next: (authResponse) => {
        const usuarioNormalizado = {
          id: authResponse.usuario.id?.toString(),
          nome: authResponse.usuario.nome,
          email: authResponse.usuario.email,
          funcao: authResponse.usuario.funcao,
          matricula: authResponse.usuario.matricula,
          imagemPerfil: authResponse.usuario.imagemPerfil
        };

        localStorage.setItem('isLogged', 'true');
        localStorage.setItem('token', authResponse.token);
        localStorage.setItem('usuario', JSON.stringify(usuarioNormalizado));

        const tipo =
          authResponse.usuario.funcao?.toLowerCase() === 'professor' ? 'professor' : 'aluno';
        localStorage.setItem('tipoUsuario', tipo);

        alert(`Bem-vindo, ${authResponse.usuario.nome}!`);
        this.router.navigate(['/home']);
      },
      error: (err) => {
        console.error('Erro ao autenticar usuário:', err);
        alert('Usuário não encontrado ou senha incorreta.');
      },
    });
  }

  onRegisterSubmit(event: Event): void {
    event.preventDefault();

    if (this.senha() !== this.confirmaSenha()) {
      alert('As senhas não coincidem.');
      return;
    }

    const novoUsuario: Usuario = {
      nome: this.nome(),
      email: this.email(),
      senha: this.senha(),
      funcao: this.selecionada().toUpperCase(),
      matricula: this.matricula(),
      imagemPerfil: this.imagemPerfilBase64 || undefined
    };

    this.usuarioService.cadastrar(novoUsuario).subscribe({
      next: (usuario) => {
        alert('Cadastro realizado! Faça login.');
        this.toggleForm();
      },
      error: (err) => {
        console.error('Erro ao cadastrar:', err);
        alert('Erro ao cadastrar usuário.');
      },
    });
  }
}

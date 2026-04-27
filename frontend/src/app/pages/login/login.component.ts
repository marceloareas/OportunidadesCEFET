import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { UsuarioService, Usuario } from '../../services/usuario.service';

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

  constructor(
    private router: Router,
    private usuarioService: UsuarioService,
    private authService: AuthService
  ) {}

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      if (!file.type.startsWith('image/')) {
        this.imagemErro = 'Selecione um arquivo de imagem válido.';
        this.imagemPerfilBase64 = null;
        this.imagemPreview = null;
        input.value = '';
        return;
      }
      const reader = new FileReader();
      reader.onload = () => {
        this.imagemPerfilBase64 = reader.result as string;
        this.imagemPreview = this.imagemPerfilBase64;
        this.imagemErro = '';
      };
      reader.readAsDataURL(file);
    } else {
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
        console.log('Usuário logado:', usuarioNormalizado.nome, '| Tipo:', tipo);
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
        console.log('Usuário cadastrado com sucesso:', usuario);
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

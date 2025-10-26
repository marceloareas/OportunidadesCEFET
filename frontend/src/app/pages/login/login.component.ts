import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
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

  constructor(private router: Router, private usuarioService: UsuarioService) {}

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

    this.usuarioService.listar().subscribe({
      next: (usuarios) => {
        const user = usuarios.find(
          (u) => u.email === this.email() && u.senha === this.senha()
        );

        if (user) {
          console.log('✅ Usuário autenticado:', user);
          alert(`Bem-vindo, ${user.nome}!`);

          const usuarioNormalizado = {
            id: user.id?.toString(),
            nome: user.nome,
            email: user.email,
            funcao: user.funcao,
            matricula: user.matricula,
          };

          localStorage.setItem('isLogged', 'true');
          localStorage.setItem('usuario', JSON.stringify(usuarioNormalizado));

          const tipo =
            user.funcao?.toLowerCase() === 'professor' ? 'professor' : 'aluno';
          localStorage.setItem('tipoUsuario', tipo);

          console.log('Usuário logado:', usuarioNormalizado.nome, '| Tipo:', tipo);
          this.router.navigate(['/home']);
        } else {
          alert('Usuário não encontrado ou senha incorreta.');
        }
      },
      error: (err) => {
        console.error('Erro ao buscar usuários:', err);
        alert('Erro ao conectar ao servidor.');
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

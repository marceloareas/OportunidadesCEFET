import { Component, ElementRef, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { PostComponent } from '../../components/post/post';
import { PostService, Post } from '../../services/post.services';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarTop, NavbarLeft, NavbarRight, PostComponent],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class Home {
  @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

  // estado de visualização
  currentView = signal<'home'>('home');
  newPostModal = signal<boolean>(false);

  // dados dos posts
  posts = signal<Post[]>([]);
  carregando = signal<boolean>(false);
  erro = signal<string>('');

  // formulário do modal
  titulo = signal<string>('');
  corpo = signal<string>('');
  imagemBase64 = signal<string | null>(null);
  imagemErro = signal<string>('');

  constructor(private postService: PostService, private router: Router) {}

  ngOnInit() {
    const isLogged = localStorage.getItem('isLogged') === 'true';
    if (!isLogged) {
      this.router.navigate(['/login']);
      return;
    }
    this.carregarPosts();
  }

  carregarPosts() {
    this.carregando.set(true);
    this.postService.getPosts().subscribe({
      next: (dados) => {
        this.posts.set(dados ?? []);
        this.carregando.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar postagens:', err);
        this.erro.set('Erro ao carregar postagens.');
        this.carregando.set(false);
      }
    });
  }

  // modal
  openNewPost() {
    this.newPostModal.set(true);
  }

  closeNewPost() {
    this.newPostModal.set(false);
  }

  onImagemSelecionada(event: Event) {
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.[0];

    if (!arquivo) {
      this.imagemBase64.set(null);
      return;
    }

    if (!arquivo.type.startsWith('image/')) {
      this.imagemErro.set('Selecione um arquivo de imagem válido.');
      input.value = '';
      return;
    }

    const leitor = new FileReader();
    leitor.onload = () => {
      this.imagemBase64.set(leitor.result as string);
      this.imagemErro.set('');
    };
    leitor.onerror = () => {
      console.error('Erro ao carregar a imagem do post.');
      this.imagemErro.set('Erro ao carregar a imagem. Tente novamente.');
      this.imagemBase64.set(null);
    };
    leitor.readAsDataURL(arquivo);
  }

  removerImagem(event?: Event) {
    event?.preventDefault();
    this.imagemBase64.set(null);
    this.imagemErro.set('');
    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  // criação de post
  enviarPost() {
    const novoPost: Post = {
      titulo: this.titulo(),
      corpo: this.corpo(),
      criadorId: 'usuarioFake',
      criado: new Date(),
      imagemBase64: this.imagemBase64() ?? undefined
    };

    this.postService.createPost(novoPost).subscribe({
      next: (p) => {
        this.posts.update(lista => [p, ...lista]);
        this.titulo.set('');
        this.corpo.set('');
        this.removerImagem();
        this.newPostModal.set(false);
      },
      error: (err) => {
        console.error('Erro ao criar post:', err);
        alert('Erro ao criar post.');
      }
    });
  }
}

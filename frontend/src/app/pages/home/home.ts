import { Component, signal } from '@angular/core';
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

  // criação de post
  enviarPost() {
    const novoPost: Post = {
      titulo: this.titulo(),
      corpo: this.corpo(),
      criadorId: 'usuarioFake',
      criado: new Date()
    };

    this.postService.createPost(novoPost).subscribe({
      next: (p) => {
        this.posts.update(lista => [p, ...lista]);
        this.titulo.set('');
        this.corpo.set('');
        this.newPostModal.set(false);
      },
      error: (err) => {
        console.error('Erro ao criar post:', err);
        alert('Erro ao criar post.');
      }
    });
  }
}

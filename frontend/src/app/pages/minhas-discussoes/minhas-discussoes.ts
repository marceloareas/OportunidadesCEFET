import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostService, Post } from '../../services/post.services';
import { FormsModule } from '@angular/forms';
import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';

@Component({
  selector: 'app-minhas-discussoes',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarTop, NavbarLeft, NavbarRight],
  templateUrl: './minhas-discussoes.html',
  styleUrls: ['./minhas-discussoes.css']
})
export class MinhasDiscussoes {
  carregando = signal(false);
  erro = signal('');
  minhasPublicacoes = signal<Post[]>([]);

  usuarioId?: string;

  constructor(private postService: PostService) {}

  ngOnInit() {
    const usuarioStr = localStorage.getItem('usuario');
    if (!usuarioStr) return;
    let parsed: any = null;
    try { parsed = JSON.parse(usuarioStr); } catch { return; }
    this.usuarioId = parsed?.id;
    if (!this.usuarioId) return;
    this.carregarDados();
  }

  private carregarDados() {
    this.carregando.set(true);
    this.erro.set('');

    this.postService.getPosts().subscribe({
      next: (posts) => {
        const meus = (posts || []).filter(p => p.criadorId === this.usuarioId);
        this.minhasPublicacoes.set(meus.map(p => ({ ...p })));
        this.carregando.set(false);
      },
      error: (err) => {
        console.error('Erro ao buscar posts:', err);
        this.erro.set('Erro ao carregar publicações.');
        this.carregando.set(false);
      }
    });
  }
}

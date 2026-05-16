import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostService, Post } from '../../services/post.services';
import { FormsModule } from '@angular/forms';
import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { PostComponent } from '../../components/post/post';
import { NavbarRight } from '../../components/navbar-right/navbar-right';

@Component({
  selector: 'app-minhas-discussoes',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarTop, NavbarLeft, NavbarRight, PostComponent],
  templateUrl: './minhas-discussoes.html',
  styleUrls: ['./minhas-discussoes.css']
})
export class MinhasDiscussoes {
  carregando = signal(false);
  paginaAtual = signal(0);
  tamanhoPagina = signal(10);
  totalPaginas = signal(0);
  erro = signal('');
  minhasPublicacoes = signal<Post[]>([]);

  usuarioId?: string;

  hasMore = computed(() => this.paginaAtual() < this.totalPaginas() - 1);

  constructor(private postService: PostService) {}

  ngOnInit() {
    const usuarioStr = localStorage.getItem('usuario');
    if (!usuarioStr) return;

    let parsed: any = null;
    try {
      parsed = JSON.parse(usuarioStr);
    } catch {
      return;
    }

    this.usuarioId = parsed?.id;
    if (!this.usuarioId) return;

    this.carregarDados(0, false);
  }

  carregarMaisPublicacoes() {
    if (this.carregando() || !this.hasMore()) return;
    this.carregarDados(this.paginaAtual() + 1, true);
  }

  private carregarDados(page = 0, append = false) {
    this.carregando.set(true);
    this.erro.set('');

    this.postService
      .getPostsByUser(this.usuarioId!, page, this.tamanhoPagina())
      .subscribe({
        next: (res) => {
          const novosPosts = res.content || [];

          this.minhasPublicacoes.set(
            append
              ? [...this.minhasPublicacoes(), ...novosPosts]
              : novosPosts
          );

          this.paginaAtual.set(res.number);
          this.totalPaginas.set(res.totalPages || 0);
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
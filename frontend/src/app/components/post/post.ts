import { Component, Input, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OportunidadeService } from '../../services/oportunidade.service';
import { PostService } from '../../services/post.services';
import { ComentarioService } from '../../services/comentario.service';
import { UsuarioService } from '../../services/usuario.service';
import { forkJoin } from 'rxjs';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-post',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './post.html',
  styleUrl: './post.css'
})
export class PostComponent {
  @Input() post!: {
    id?: string;
    titulo: string;
    corpo: string;
    criadorId?: string;
    nomeCriador?: string;
    criado?: string | Date;
    idLikes?: string[];
    idComentarios?: any[];
    imagemBase64?: string;
    vagasPreenchidas?: number;
    quantidadeDeVagas?: number;
    ehOportunidade?: boolean;
    alunosCandidatosId?: string[];
  };

  tipoUsuario = signal<string>(localStorage.getItem('tipoUsuario') || 'aluno');
  usuarioLogado = signal<{ id: string; nome: string; funcao: string } | null>(null);

  novoComentario = '';
  comentarios: Array<{ autor: string; texto: string; data?: string; id?: string }> = [];
  
  jaCandidatado = signal<boolean>(false);
  contadorCandidatos = signal<number>(0);
  curtiu = signal<boolean>(false);
  contadorLikes = signal<number>(0);
  newCommentModal = signal<boolean>(false);
  
  // use `inject` for ComentarioService to avoid constructor DI ordering issues in standalone components
  private comentarioService = inject(ComentarioService);
  // inject usuario service to resolve nomes dos autores
  private usuarioService = inject(UsuarioService);

  constructor(
    private oportunidadeService: OportunidadeService,
    private postService: PostService,
  ) {
    const usuario = localStorage.getItem('usuario');
    if (usuario) {
      try {
        const parsed = JSON.parse(usuario);
        this.usuarioLogado.set({
          id: parsed.id,
          nome: parsed.nome,
          funcao: parsed.funcao?.toUpperCase() || 'ALUNO'
        });
      } catch {
        this.usuarioLogado.set(null);
      }
    }
  }

  ngOnInit() {
    this.contadorCandidatos.set(this.post.alunosCandidatosId?.length ?? 0);
    this.contadorLikes.set(this.post.idLikes?.length ?? 0);

    const usuario = this.usuarioLogado();
    if (usuario) {
      if (this.post.idLikes?.includes(usuario.id)) this.curtiu.set(true);
      if (this.post.alunosCandidatosId?.includes(usuario.id)) this.jaCandidatado.set(true);
    }
  }

  get dataFormatada(): string {
    if (!this.post.criado) return '';
    const data = new Date(this.post.criado);
    return data.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
  
  openNewComment() {
    this.newCommentModal.set(true);
    // carregar comentários do backend quando abrir o modal
    this.loadComments();
  }

  closeNewComment() {
    this.newCommentModal.set(false);
  }

  enviarComentario() {
  // if (!this.novoComentario.trim()) return;
  const usuario = this.usuarioLogado();
  if (!usuario || !this.post.id) {
    alert('Usuário ou post não identificado.');
    return;
  }

    const payload = {
      usuarioId: usuario.id,
      tipoEntidadePai: this.post.ehOportunidade ? 'Oportunidade' : 'Post',
      idPost: this.post.id,
      texto: this.novoComentario,
      idComentarioPai: null
    } as any;

    console.log('Enviando comentário payload:', payload);

  this.comentarioService.criar(payload).subscribe({
      next: (saved) => {
        console.log('Comentário salvo:', saved);
        // adicionar no topo da lista de comentários exibida
        this.comentarios.unshift({
          autor: usuario.nome || usuario.id,
          texto: saved.texto || this.novoComentario,
          data: saved.dataComentario as any,
          id: saved.id
        });

  // atualizar contagem/local do post se necessário
        if (!this.post.idComentarios) this.post.idComentarios = [];
        if (saved.id) this.post.idComentarios.unshift(saved.id);

        this.novoComentario = '';
      },
      error: (err) => {
        console.error('Erro ao enviar comentário:', err);
        alert('Erro ao enviar comentário. Veja o console para mais detalhes.');
      }
    });
}

  loadComments() {
    if (!this.post?.id) return;
  this.comentarioService.listarComentariosPost(this.post.id).subscribe({
      next: (arr) => {
        // mapear e buscar nomes dos autores (evitar mostrar apenas IDs)
        const uniqueIds = Array.from(new Set(arr.map((c) => c.usuarioId).filter(Boolean)));

        if (uniqueIds.length === 0) {
          this.comentarios = arr.map((c) => ({
            autor: 'Anônimo',
            texto: c.texto || '',
            data: c.dataComentario ? new Date(c.dataComentario).toLocaleString() : undefined,
            id: c.id
          }));
          // atualizar ids do post para que o contador reflita a quantidade
          try { this.post.idComentarios = arr.map((c) => c.id); } catch {}
          return;
        }

        const requests = uniqueIds.map((id) => this.usuarioService.buscarPorId(id!));

        forkJoin(requests).subscribe({
          next: (users) => {
            const nameById: Record<string, string> = {};
            users.forEach((u) => {
              if (u && u.id) nameById[u.id] = u.nome;
            });

            this.comentarios = arr.map((c) => ({
              autor: (c.usuarioId && nameById[c.usuarioId]) || 'Anônimo',
              texto: c.texto || '',
              data: c.dataComentario ? new Date(c.dataComentario).toLocaleString() : undefined,
              id: c.id
            }));
            // atualizar ids do post para que o contador reflita a quantidade
            try { this.post.idComentarios = arr.map((c) => c.id); } catch {}
          },
          error: (err) => {
            console.warn('Erro ao buscar usuários dos comentários:', err);
            // fallback: mostrar ids ou 'Anônimo'
            this.comentarios = arr.map((c) => ({
              autor: c.usuarioId || 'Anônimo',
              texto: c.texto || '',
              data: c.dataComentario ? new Date(c.dataComentario).toLocaleString() : undefined,
              id: c.id
            }));
            try { this.post.idComentarios = arr.map((c) => c.id); } catch {}
          }
        });
      },
      error: (err) => {
        console.warn('Erro ao carregar comentários:', err);
      }
    });
  }

  alternarLike() {
    const usuario = this.usuarioLogado();
    if (!usuario || !this.post.id) return;

    const jaCurtiu = this.curtiu();
    this.curtiu.set(!jaCurtiu);

    if (jaCurtiu) {
      this.contadorLikes.update((n) => n - 1);
      this.post.idLikes = this.post.idLikes?.filter((id) => id !== usuario.id) || [];
    } else {
      this.contadorLikes.update((n) => n + 1);
      if (!this.post.idLikes) this.post.idLikes = [];
      this.post.idLikes.push(usuario.id);
    }

      const req = this.post.ehOportunidade
      ? this.oportunidadeService.curtirOportunidade(this.post.id, usuario.id)
      : this.postService.atualizarLike(this.post.id, usuario.id);

    req.subscribe({
      error: (err) => console.warn('Erro ao curtir:', err)
    });
  }

  candidatar() {
    const usuario = this.usuarioLogado();
    if (!usuario || !this.post.id) {
      alert('Usuário não identificado.');
      return;
    }

    this.oportunidadeService.candidatarAluno(this.post.id, usuario.id).subscribe({
      next: () => {
        alert('Candidatura realizada com sucesso!');
        this.jaCandidatado.set(true);
        this.contadorCandidatos.update((n) => n + 1);

        if (!this.post.alunosCandidatosId) this.post.alunosCandidatosId = [];
        this.post.alunosCandidatosId.push(usuario.id);
      },
      error: (err) => {
        console.error('Erro ao se candidatar:', err);
        alert('Erro ao se candidatar à vaga.');
      }
    });
  }
}

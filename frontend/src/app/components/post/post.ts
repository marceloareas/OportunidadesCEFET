import { Component, Input, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { OportunidadeService } from '../../services/oportunidade.service';
import { PostService } from '../../services/post.services';
import { ComentarioService } from '../../services/comentario.service';
import { UsuarioService, Usuario } from '../../services/usuario.service';

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
    finalizada?: boolean;
    vagasPreenchidas?: number;
    quantidadeDeVagas?: number;
    ehOportunidade?: boolean;
    alunosCandidatosId?: string[];
    alunosAprovadosId?: string[];
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
  candidatosModal = signal<boolean>(false);
  candidatos = signal<Usuario[]>([]);
  candidatosCarregando = signal<boolean>(false);
  candidatosErro = signal<string>('');
  aprovando = signal<string | null>(null);
  somenteAprovados = signal<boolean>(false);

  private comentarioService = inject(ComentarioService);
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
    this.loadComments();
  }

  closeNewComment() {
    this.newCommentModal.set(false);
  }

  enviarComentario() {
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

    this.comentarioService.criar(payload).subscribe({
      next: (saved) => {
        this.comentarios.unshift({
          autor: usuario.nome || usuario.id,
          texto: saved.texto || this.novoComentario,
          data: saved.dataComentario as any,
          id: saved.id
        });

        if (!this.post.idComentarios) this.post.idComentarios = [];
        if (saved.id) this.post.idComentarios.unshift(saved.id);

        this.novoComentario = '';
      },
      error: (err) => {
        console.error('Erro ao enviar comentario:', err);
        alert('Erro ao enviar comentario. Veja o console para mais detalhes.');
      }
    });
  }

  loadComments() {
    if (!this.post?.id) return;
    const req = this.post.ehOportunidade
      ? this.comentarioService.listarComentariosOportunidade(this.post.id)
      : this.comentarioService.listarComentariosPost(this.post.id);

    req.subscribe({
      next: (arr) => {
        const uniqueIds = Array.from(new Set(arr.map((c) => c.usuarioId).filter(Boolean)));

        if (uniqueIds.length === 0) {
          this.comentarios = arr.map((c) => ({
            autor: 'Anônimo',
            texto: c.texto || '',
            data: c.dataComentario ? new Date(c.dataComentario).toLocaleString() : undefined,
            id: c.id
          }));
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
            try { this.post.idComentarios = arr.map((c) => c.id); } catch {}
          },
          error: (err) => {
            console.warn('Erro ao buscar usuarios dos comentarios:', err);
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
        console.warn('Erro ao carregar comentarios:', err);
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

  podeVerCandidatos(): boolean {
    const usuario = this.usuarioLogado();
    return Boolean(
      usuario &&
      this.post.ehOportunidade &&
      this.post.criadorId &&
      usuario.id === this.post.criadorId
    );
  }

  abrirCandidatos() {
    if (!this.podeVerCandidatos() || !this.post.id) return;
    const usuario = this.usuarioLogado();
    if (!usuario) return;

    this.candidatosCarregando.set(true);
    this.candidatosErro.set('');

    this.oportunidadeService
      .listarCandidatosDoProfessor(this.post.id, usuario.id)
      .subscribe({
        next: (lista) => {
          this.candidatos.set(lista || []);
          this.candidatosModal.set(true);
          this.candidatosCarregando.set(false);
        },
        error: (err) => {
          console.error('Erro ao listar candidatos:', err);
          this.candidatosErro.set('Não foi possível carregar candidatos.');
          this.candidatosCarregando.set(false);
          this.candidatosModal.set(true);
        }
      });
  }

  fecharCandidatos() {
    this.candidatosModal.set(false);
  }

  candidatoAprovado(idAluno: string | undefined): boolean {
    if (!idAluno) return false;
    return this.post.alunosAprovadosId?.includes(idAluno) ?? false;
  }

  candidatosFiltrados(): Usuario[] {
    const lista = this.candidatos() || [];
    if (!this.somenteAprovados()) return lista;
    const aprovados = new Set(this.post.alunosAprovadosId || []);
    return lista.filter((c) => c.id && aprovados.has(c.id));
  }

  vagasRestantes(): number {
    const total = this.post.quantidadeDeVagas ?? 0;
    const preenchidas = this.post.vagasPreenchidas ?? 0;
    return Math.max(0, total - preenchidas);
  }

  aprovarCandidato(candidato: Usuario) {
    const usuario = this.usuarioLogado();
    const postId = this.post.id;
    const candidatoId = candidato.id;
    if (!usuario || !postId || !candidatoId) return;
    if (this.vagasRestantes() <= 0) {
      alert('Todas as vagas já foram preenchidas.');
      return;
    }
    this.aprovando.set(candidatoId);
    this.oportunidadeService
      .aprovarCandidatoDoProfessor(postId, candidatoId, usuario.id)
      .subscribe({
        next: (op) => {
          if (!this.post.alunosAprovadosId) this.post.alunosAprovadosId = [];
          this.post.alunosAprovadosId.push(candidatoId);
          this.post.vagasPreenchidas = op.vagasPreenchidas ?? this.post.vagasPreenchidas;
          this.post.finalizada = op.finalizada ?? this.post.finalizada;
          this.aprovando.set(null);
          alert('Candidato aprovado e vaga atribuída.');
        },
        error: (err) => {
          console.error('Erro ao aprovar candidato:', err);
          alert('Não foi possível aprovar o candidato.');
          this.aprovando.set(null);
        }
      });
  }
}

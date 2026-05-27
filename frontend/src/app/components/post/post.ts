import { Component, Input, signal, inject, Output, EventEmitter } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { OportunidadeService } from '../../services/oportunidade.service';
import { PostService } from '../../services/post.services';
import { ComentarioService } from '../../services/comentario.service';
import { UsuarioService, Usuario } from '../../services/usuario.service';
import { FeedItem } from '../../services/feed.service';
import { SavedItemsService } from '../../services/itens-salvos.service';

@Component({
  selector: 'app-post',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './post.html',
  styleUrl: './post.css',
})
export class PostComponent {
  @Input() post!: FeedItem;
  @Output() abrirModalCandidatos = new EventEmitter<any>();
  readonly comentariosPorPagina = 10;

  private readonly categoriaLabels: Record<string, string> = {
    MONITORIA: 'Monitoria',
    EXTENSAO: 'Extensão',
    PESQUISA: 'Pesquisa',
    ESTAGIO: 'Estágio',
    ORIENTACAO_TCC: 'Orientação TCC',
  };

  private readonly areaLabels: Record<string, string> = {
    CIENCIAS_AGRARIAS: 'Ciências Agrárias',
    CIENCIAS_BIOLOGICAS: 'Ciências Biológicas',
    CIENCIAS_DA_SAUDE: 'Ciências da Saúde',
    CIENCIAS_EXATAS_E_DA_TERRA: 'Ciências Exatas e da Terra',
    ENGENHARIAS: 'Engenharias',
    CIENCIAS_HUMANAS: 'Ciências Humanas',
    CIENCIAS_SOCIAIS_APLICADAS: 'Ciências Sociais Aplicadas',
    LINGUISTICA_LETRAS_E_ARTES: 'Linguística, Letras e Artes',
    MULTIDISCIPLINAR: 'Multidisciplinar',
  };

  tipoUsuario = signal<string>(localStorage.getItem('tipoUsuario') || 'aluno');
  usuarioLogado = signal<{
    id: string;
    nome: string;
    funcao: string;
    imagemPerfil?: string;
  } | null>(null);

  novoComentario = '';
  comentarios: Array<{ autor: string; imagemPerfil?: string; texto: string; data?: string; id?: string; usuarioId?: string }> = [];
  paginaComentarios = signal<number>(1);

  jaCandidatado = signal<boolean>(false);
  contadorCandidatos = signal<number>(0);
  curtiu = signal<boolean>(false);
  contadorLikes = signal<number>(0);
  contadorComentarios = signal<number>(0);

  newCommentModal = signal<boolean>(false);
  candidatosModal = signal<boolean>(false);
  candidatos = signal<Usuario[]>([]);
  candidatosCarregando = signal<boolean>(false);
  candidatosErro = signal<string>('');
  aprovando = signal<string | null>(null);
  somenteAprovados = signal<boolean>(false);

  salvo = signal<boolean>(false);

  private comentarioService = inject(ComentarioService);
  private usuarioService = inject(UsuarioService);
  private savedService = inject(SavedItemsService);

  constructor(
      private oportunidadeService: OportunidadeService,
      private postService: PostService,
      private router: Router
    ) {
      const usuario = localStorage.getItem('usuario');
      if (usuario) {
        try {
          const parsed = JSON.parse(usuario);
          this.usuarioLogado.set({
            id: parsed.id,
            nome: parsed.nome,
            funcao: parsed.funcao?.toUpperCase() || 'ALUNO',
            imagemPerfil: parsed.imagemPerfil // <-- ADICIONE ESTA LINHA
          });
        } catch {
          this.usuarioLogado.set(null);
        }
      }
    }

  ngOnInit() {
    this.contadorCandidatos.set(this.post.alunosCandidatosId?.length ?? 0);
    this.contadorLikes.set(this.post.idLikes?.length ?? 0);
    this.contadorComentarios.set(this.post.idComentarios?.length ?? 0);

    if (!this.post.idLikes) this.post.idLikes = [];
    if (!this.post.idComentarios) this.post.idComentarios = [];
    if (!this.post.alunosCandidatosId) this.post.alunosCandidatosId = [];
    if (!this.post.alunosAprovadosId) this.post.alunosAprovadosId = [];

    const usuario = this.usuarioLogado();
    
    if (usuario) {
      if (this.post.idLikes?.includes(usuario.id)) this.curtiu.set(true);
      if (this.post.alunosCandidatosId?.includes(usuario.id)) this.jaCandidatado.set(true);
    }

    if (usuario && this.post.id) {
      this.savedService.listarPorUsuario(usuario.id).subscribe(res => {
        const ids = res.content.map(item => item.id);
        if (ids.includes(this.post.id!)) {
          this.salvo.set(true);
        }
      });
    }
  }

  get dataFormatada(): string {
    if (!this.post.createdAt) return '';
    const data = new Date(this.post.createdAt);
    return data.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
  
  formatarDataComentario(data?: string | Date): string {
    if (!data) return '';

    const createdAt = new Date(data);
    if (Number.isNaN(createdAt.getTime())) return '';

    const dia = String(createdAt.getDate()).padStart(2, '0');
    const mes = String(createdAt.getMonth() + 1).padStart(2, '0');
    const ano = createdAt.getFullYear();
    const horas = String(createdAt.getHours()).padStart(2, '0');
    const minutos = String(createdAt.getMinutes()).padStart(2, '0');

    return `${dia}/${mes}/${ano} ${horas}:${minutos}`;
  }

  irParaPerfilCriador() {
    const idUsuario = this.post?.criadorId;

    if (!idUsuario) {
      console.error('ID do criador do post não encontrado.');
      return;
    }

    this.router.navigate(['/perfil', idUsuario]);
  }
    

  openNewComment() {
    this.newCommentModal.set(true);
    this.loadComments();
  }

  closeNewComment() {
    this.newCommentModal.set(false);
  }

  comentariosDaPaginaAtual(): Array<{ autor: string; imagemPerfil?: string; texto: string; data?: string; id?: string; usuarioId?: string }> {
    const inicio = (this.paginaComentarios() - 1) * this.comentariosPorPagina;
    return this.comentarios.slice(inicio, inicio + this.comentariosPorPagina);
  }

  totalPaginasComentarios(): number {
    return Math.max(1, Math.ceil(this.comentarios.length / this.comentariosPorPagina));
  }

  irParaPaginaComentarios(pagina: number) {
    const paginaValida = Math.min(Math.max(1, pagina), this.totalPaginasComentarios());
    this.paginaComentarios.set(paginaValida);
  }

  paginaAnteriorComentarios() {
    this.irParaPaginaComentarios(this.paginaComentarios() - 1);
  }

  proximaPaginaComentarios() {
    this.irParaPaginaComentarios(this.paginaComentarios() + 1);
  }

  enviarComentario() {
    const usuario = this.usuarioLogado();
    if (!usuario || !this.post.id) {
      alert('Usuário ou post não identificado.');
      return;
    }

    if (this.post.tipo === 'OPORTUNIDADE' && this.post.status === 'FINALIZADA') {
      alert('Esta oportunidade está finalizada e não aceita mensagens.');
      return;
    }

    const textoComentario = this.novoComentario.trim();
    if (!textoComentario) {
      alert('Comentário não pode ser vazio.');
      return;
    }

    const payload = {
      usuarioId: usuario.id,
      tipoEntidadePai: this.post.tipo,
      idPost: this.post.referenciaId || this.post.id,
      texto: textoComentario
    } as any;

    this.comentarioService.criar(payload).subscribe({
      next: (saved) => {
        this.comentarios.push({
          autor: usuario.nome || usuario.id,
          imagemPerfil: usuario.imagemPerfil,
          texto: saved.texto || this.novoComentario,
          data: saved.createdAt as string | undefined,
          id: saved.id,
          usuarioId: usuario.id
        });

        if (!this.post.idComentarios) this.post.idComentarios = [];
        if (saved.id) this.post.idComentarios.push(saved.id);

        this.contadorComentarios.update(n => n + 1);
        this.irParaPaginaComentarios(this.totalPaginasComentarios());

        this.novoComentario = '';
      },
      error: (err) => {
        console.error('Erro ao enviar comentario:', err);
        alert('Erro ao enviar comentario. Veja o console para mais detalhes.');
      },
    });
  }

  loadComments() {
    const referenciaId = this.post.referenciaId || this.post.id;
    if (!referenciaId) return;

    const req =
      this.post.tipo === 'OPORTUNIDADE'
        ? this.comentarioService.listarComentariosOportunidade(referenciaId)
        : this.comentarioService.listarComentariosPost(referenciaId);

    req.subscribe({
      next: (arr) => {
        this.contadorComentarios.set(arr.length);
        this.paginaComentarios.set(1);

        const uniqueIds = Array.from(new Set(arr.map((c) => c.usuarioId).filter(Boolean)));

        if (uniqueIds.length === 0) {
          this.comentarios = arr.map((c) => ({
            autor: 'Anônimo',
            imagemPerfil: undefined,
            texto: c.texto || '',
            data: c.createdAt as string | undefined,
            id: c.id,
            usuarioId: c.usuarioId
          }));
          try {
            this.post.idComentarios = arr
              .map((c) => c.id)
              .filter((id): id is string => Boolean(id));
          } catch {}
          return;
        }

        const requests = uniqueIds.map((id) => this.usuarioService.buscarPorId(id!));

        forkJoin(requests).subscribe({
          next: (users) => {
            const userById: Record<string, { nome: string; imagemPerfil?: string }> = {};
            users.forEach((u) => {
              if (u && u.id) userById[u.id] = { nome: u.nome, imagemPerfil: u.imagemPerfil };
            });

            this.comentarios = arr.map((c) => ({
              autor: (c.usuarioId && userById[c.usuarioId]?.nome) || 'Anônimo',
              imagemPerfil: (c.usuarioId && userById[c.usuarioId]?.imagemPerfil) || undefined,
              texto: c.texto || '',
              data: c.createdAt as string | undefined,
              id: c.id,
              usuarioId: c.usuarioId
            }));
            try {
              this.post.idComentarios = arr
                .map((c) => c.id)
                .filter((id): id is string => Boolean(id));
            } catch {}
          },
          error: (err) => {
            console.warn('Erro ao buscar usuarios dos comentarios:', err);
            this.comentarios = arr.map((c) => ({
              autor: c.usuarioId || 'Anônimo',
              imagemPerfil: undefined,
              texto: c.texto || '',
              data: c.createdAt as string | undefined,
              id: c.id,
              usuarioId: c.usuarioId
            }));
            try {
              this.post.idComentarios = arr
                .map((c) => c.id)
                .filter((id): id is string => Boolean(id));
            } catch {}
          },
        });
      },
      error: (err) => {
        console.warn('Erro ao carregar comentarios:', err);
      },
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

    const referenciaId = this.post.referenciaId || this.post.id;
    if (!referenciaId) return;

    const req =
      this.post.tipo === 'OPORTUNIDADE'
        ? this.oportunidadeService.curtirOportunidade(referenciaId, usuario.id)
        : this.postService.atualizarLike(referenciaId, usuario.id);

    req.subscribe({
      error: (err) => console.warn('Erro ao curtir:', err),
    });
  }

  candidatar() {
    const usuario = this.usuarioLogado();
    if (!usuario || !this.post.id) {
      alert('Usuário não identificado.');
      return;
    }

    if (!this.podeCandidatar()) {
      alert('As inscrições não estão abertas para esta oportunidade.');
      return;
    }

    const referenciaId = this.post.referenciaId || this.post.id;
    if (!referenciaId) return;

    this.oportunidadeService.candidatarAluno(referenciaId, usuario.id).subscribe({
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
      },
    });
  }

  statusDescricao(): string {
    switch (this.post.status) {
      case 'INSCRICOES_EM_BREVE':
        return 'Inscrições em breve';
      case 'INSCRICOES_ABERTAS':
        return 'Inscrições abertas';
      case 'INSCRICOES_ENCERRADAS':
        return 'Inscrições encerradas';
      case 'FINALIZADA':
        return 'Finalizada';
      default:
        return this.post.finalizada ? 'Finalizada' : 'Status indefinido';
    }
  }

  periodoInscricao(): string {
    const inicio = this.post.dataInicioInscricao ? new Date(this.post.dataInicioInscricao) : null;
    const fim = this.post.dataFimInscricao ? new Date(this.post.dataFimInscricao) : null;

    if (!inicio || !fim || Number.isNaN(inicio.getTime()) || Number.isNaN(fim.getTime())) {
      return '';
    }

    return `${inicio.toLocaleDateString('pt-BR')} até ${fim.toLocaleDateString('pt-BR')}`;
  }

  categoriaDescricao(): string {
    if (!this.post.idCategoria) return 'Categoria não informada';
    return this.categoriaLabels[this.post.idCategoria] || this.post.idCategoria;
  }

  areasDescricao(): string {
    const areas = this.post.grandesAreas || [];

    if (areas.length === 0) return 'Área não informada';

    return areas.map((area) => this.areaLabels[area] || area).join(' · ');
  }

  podeCandidatar(): boolean {
    return this.post.tipo === 'OPORTUNIDADE' && this.tipoUsuario() === 'aluno' && this.post.status === 'INSCRICOES_ABERTAS';
  }

  mensagensBloqueadas(): boolean {
    return this.post.tipo === 'OPORTUNIDADE' && this.post.status === 'FINALIZADA';
  }

  podeVerCandidatos(): boolean {
    const usuario = this.usuarioLogado();
    return Boolean(
      usuario &&
      this.post.tipo === 'OPORTUNIDADE' &&
      this.post.criadorId &&
      usuario.id === this.post.criadorId,
    );
  }

  abrirCandidatos() {
    if (!this.podeVerCandidatos() || !this.post.id) return;
    const usuario = this.usuarioLogado();
    if (!usuario) return;

    // Emitir evento para a página pai gerenciar a seleção em lote
    this.abrirModalCandidatos.emit({ oportunidadeId: this.post.id, professorId: usuario.id });
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
    const postId = this.post.referenciaId || this.post.id;
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
        },
      });
  }

  toggleSalvar() {
    const usuario = this.usuarioLogado();
    if (!usuario || !this.post.id) return;

    if (this.salvo()) {
      this.savedService.remover(usuario.id, this.post.id).subscribe(() => {
        this.salvo.set(false);
      });
    } else {
      this.savedService.salvar(usuario.id, this.post.id).subscribe(() => {
        this.salvo.set(true);
      });
    }
  }
}

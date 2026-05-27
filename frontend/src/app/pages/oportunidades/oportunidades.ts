import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { PostComponent } from '../../components/post/post';
import { OportunidadeService, Oportunidade } from '../../services/oportunidade.service';
import { FeedItem } from '../../services/feed.service';
import { UsuarioService, Usuario } from '../../services/usuario.service';

@Component({
  selector: 'app-oportunidades',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarTop, NavbarLeft, NavbarRight, PostComponent],
  templateUrl: './oportunidades.html',
  styleUrl: './oportunidades.css'
})
export class OportunidadesPage {

  candidatosPorOportunidade = signal<Record<string, Usuario[]>>({});
  paginaAtual = signal(0);
  tamanhoPagina = signal(10);
  totalPaginas = signal(0);

  totalPaginasCandidatos = signal(0);
  paginaAtualCandidatos = signal(0);

  usuarioId?: string;
  usuarioFuncao?: string;

  minhasOportunidades = signal<Oportunidade[]>([]);

  carregando = signal<boolean>(false);
  erro = signal<string>('');

  // modal candidatos
  mostrandoListaCandidatos = signal<boolean>(false);
  oportunidadeSelecionadaId = signal<string | null>(null);
  somenteAprovados = signal<boolean>(false);
  aprovando = signal<string | null>(null);
  candidatosErro = signal<string>('');
  candidatosCarregando = signal<boolean>(false);
  candidaturas = signal<Oportunidade[]>([]);

  // seleção em lote e modal de confirmação
  candidatosSelecionados = signal<Set<string>>(new Set());
  mostrandoModalConfirmacao = signal<boolean>(false);
  finalizandoOportunidade = signal<boolean>(false);

  constructor(
    private oportunidadeService: OportunidadeService,
    private usuarioService: UsuarioService,
    private router: Router
  ) {}

    ngOnInit() {
      if (typeof window === 'undefined') return;

      const usuarioStr = localStorage.getItem('usuario');
      if (!usuarioStr) return;

      let parsed: any = null;
      try {
        parsed = JSON.parse(usuarioStr);
      } catch {
        return;
      }

      this.usuarioId = parsed?.id;
      this.usuarioFuncao = parsed?.funcao?.toLowerCase();

      if (!this.usuarioId) return;

      this.carregarDados(0, false);
    }

    carregarMaisOportunidades() {
      if (this.carregando() || !this.hasMoreOportunidades()) return;
      this.carregarDados(this.paginaAtual() + 1, true);
    }

    hasMoreOportunidades = computed(
      () => this.paginaAtual() < this.totalPaginas() - 1
    );

    private carregarDados(page = 0, append = false) {
      this.carregando.set(true);
      this.erro.set('');

      const request =
        this.usuarioFuncao === 'professor'
          ? this.oportunidadeService.listarPorProfessor(
              this.usuarioId!,
              page,
              this.tamanhoPagina()
            )
          : this.oportunidadeService.listarPorAluno(
              this.usuarioId!,
              page,
              this.tamanhoPagina()
            );

      request.subscribe({
          next: (res) => {
            const novas = res.content || [];
            console.log('candidaturas:', novas);

          if (this.usuarioFuncao === 'professor') {
            this.minhasOportunidades.set(
              append
                ? [...this.minhasOportunidades(), ...novas]
                : novas
            );
          } else {
            this.candidaturas.set(
              append
                ? [...this.candidaturas(), ...novas]
                : novas
            );
          }

          this.paginaAtual.set(res.number);
          this.totalPaginas.set(res.totalPages || 0);
          this.carregando.set(false);
        },
        error: (err) => {
          console.error('Erro ao carregar oportunidades:', err);
          this.erro.set('Erro ao carregar oportunidades.');
          this.carregando.set(false);
        }
      });
    }

    abrirListaCandidatos(op: Oportunidade) {
      if (!op.id) return;

      this.oportunidadeSelecionadaId.set(op.id);
      this.paginaAtualCandidatos.set(0);
      this.candidatosSelecionados.set(new Set()); // Resetar seleção
      this.mostrandoListaCandidatos.set(true);

      this.carregarCandidatos(op.id);
    }

  fecharListaCandidatos() {
    this.mostrandoListaCandidatos.set(false);
    this.oportunidadeSelecionadaId.set(null);
    this.somenteAprovados.set(false);
    this.candidatosSelecionados.set(new Set());
    this.mostrandoModalConfirmacao.set(false);
  }

  carregarCandidatos(opId: string) {
    if (!this.usuarioId) return;
    
    this.candidatosCarregando.set(true);
    this.candidatosErro.set('');

    this.oportunidadeService
      .listarCandidatosDoProfessor(opId, this.usuarioId)
      .subscribe({
        next: (lista: Usuario[]) => {
          this.candidatosPorOportunidade.update(m => ({
            ...m,
            [opId]: lista || []
          }));

          this.totalPaginasCandidatos.set(1);
          this.candidatosCarregando.set(false);
        },
        error: (err) => {
          console.error('Erro ao carregar candidatos:', err);
          this.candidatosErro.set('Erro ao carregar candidatos');
          this.candidatosCarregando.set(false);
        }
      });
  }

  candidatosFiltrados(): Usuario[] {
    const opId = this.oportunidadeSelecionadaId();
    if (!opId) return [];
    const lista = this.candidatosPorOportunidade()[opId] || [];

    if (!this.somenteAprovados()) return lista;

    const aprovados = new Set(
      this.minhasOportunidades().find(o => o.id === opId)?.alunosAprovadosId || []
    );

    return lista.filter(c => c.id && aprovados.has(c.id));
  }

  proximaPaginaCandidatos() {
    if (this.paginaAtualCandidatos() < this.totalPaginasCandidatos() - 1) {
      this.paginaAtualCandidatos.update(v => v + 1);
      const opId = this.oportunidadeSelecionadaId();
      if (opId) this.carregarCandidatos(opId);
    }
  }

  paginaAnteriorCandidatos() {
    if (this.paginaAtualCandidatos() > 0) {
      this.paginaAtualCandidatos.update(v => v - 1);
      const opId = this.oportunidadeSelecionadaId();
      if (opId) this.carregarCandidatos(opId);
    }
  }

  estaAprovado(candId: string | undefined): boolean {
    const opId = this.oportunidadeSelecionadaId();
    if (!opId || !candId) return false;
    const aprovados = this.minhasOportunidades().find(o => o.id === opId)?.alunosAprovadosId || [];
    return aprovados.includes(candId);
  }

  aprovarCandidato(c: Usuario) {
    const opId = this.oportunidadeSelecionadaId();
    if (!opId || !c.id || !this.usuarioId) return;
    this.aprovando.set(c.id);
    this.oportunidadeService.aprovarCandidatoDoProfessor(opId, c.id, this.usuarioId).subscribe({
      next: (op) => {
        // atualiza listas
        this.minhasOportunidades.update(list =>
          list.map(o => o.id === op.id ? op : o)
        );
        this.aprovando.set(null);
      },
      error: (err) => {
        console.error('Erro ao aprovar candidato:', err);
        alert('Não foi possível aprovar o candidato.');
        this.aprovando.set(null);
      }
    });
  }

  statusAluno(op: Oportunidade): string {
    if (!this.usuarioId) return 'Pendente';
    if ((op.alunosAprovadosId || []).includes(this.usuarioId)) return 'Aprovado';
    return 'Pendente';
  }

  finalizar(op: Oportunidade) {
    if (!op.id || op.finalizada) return;
    if (!confirm('Deseja finalizar esta oportunidade? Isso impede novas candidaturas.')) return;
    this.oportunidadeService.finalizarOportunidade(op.id).subscribe({
      next: (atualizada) => {
        this.minhasOportunidades.update(list =>
          list.map(o => o.id === atualizada.id ? atualizada : o)
        );
      },
      error: (err) => {
        console.error('Erro ao finalizar oportunidade:', err);
        alert('Não foi possível finalizar.');
      }
    });
  }

  novaOportunidade() {
    this.router.navigate(['/home'], { queryParams: { modo: 'oportunidade' } });
  }

  statusDescricao(status?: Oportunidade['status']): string {
    switch (status) {
      case 'INSCRICOES_EM_BREVE':
        return 'Inscrições em breve';
      case 'INSCRICOES_ABERTAS':
        return 'Inscrições abertas';
      case 'INSCRICOES_ENCERRADAS':
        return 'Inscrições encerradas';
      case 'FINALIZADA':
        return 'Finalizada';
      default:
        return 'Status indefinido';
    }
  }

  periodoInscricao(op: Oportunidade): string {
    const inicio = op.dataInicioInscricao ? new Date(op.dataInicioInscricao) : null;
    const fim = op.dataFimInscricao ? new Date(op.dataFimInscricao) : null;

    if (!inicio || !fim || Number.isNaN(inicio.getTime()) || Number.isNaN(fim.getTime())) {
      return 'Período não informado';
    }

    return `${inicio.toLocaleDateString('pt-BR')} até ${fim.toLocaleDateString('pt-BR')}`;
  }

  oportunidadeComoFeedItem(op: Oportunidade): FeedItem {
    return {
      id: op.id,
      referenciaId: op.id,
      tipo: 'OPORTUNIDADE',
      titulo: op.nome,
      corpo: op.descricao,
      criadorId: op.professorId,
      nomeCriador: op.nomeCriador,
      createdAt: (op as any).criado ?? op.createdAt,
      imagemBase64: op.imagemBase64,
      imagemPerfil: op.imagemPerfil,
      idLikes: op.idLikes || [],
      dataInicioInscricao: op.dataInicioInscricao,
      dataFimInscricao: op.dataFimInscricao,
      idCategoria: op.idCategoria,
      grandesAreas: op.grandesAreas,
      quantidadeDeVagas: op.quantidadeDeVagas,
      vagasPreenchidas: op.vagasPreenchidas,
      finalizada: op.finalizada,
      status: op.status,
      alunosCandidatosId: op.alunosCandidatosId,
      alunosAprovadosId: op.alunosAprovadosId,
    };
  }

  // Seleção em lote
  toggleCandidato(candId: string | undefined) {
    if (!candId) return;
    const selecionados = new Set(this.candidatosSelecionados());
    if (selecionados.has(candId)) {
      selecionados.delete(candId);
    } else {
      selecionados.add(candId);
    }
    this.candidatosSelecionados.set(selecionados);
  }

  isCandidatoSelecionado(candId: string | undefined): boolean {
    return candId ? this.candidatosSelecionados().has(candId) : false;
  }

  abrirModalConfirmacao() {
    if (this.candidatosSelecionados().size === 0) {
      alert('Selecione pelo menos um aluno para finalizar.');
      return;
    }
    this.mostrandoModalConfirmacao.set(true);
  }

  fecharModalConfirmacao() {
    this.mostrandoModalConfirmacao.set(false);
  }

  confirmarFinalizacao() {
    const opId = this.oportunidadeSelecionadaId();
    if (!opId || !this.usuarioId) return;

    const selecionados = Array.from(this.candidatosSelecionados());
    if (selecionados.length === 0) return;

    this.finalizandoOportunidade.set(true);
    this.mostrandoModalConfirmacao.set(false);

    // Aprovar todos os selecionados
    const aprovacoes = selecionados.map(idAluno =>
      this.oportunidadeService.aprovarCandidatoDoProfessor(opId, idAluno, this.usuarioId!)
    );

    // Usar Promise.all para aprovar todos em paralelo
    Promise.all(aprovacoes.map(obs => obs.toPromise())).then(
      (resultados: any[]) => {
        // Atualizar a oportunidade com o último resultado válido
        const ultimaOp = resultados.find(r => r && r.id);
        if (ultimaOp) {
          this.minhasOportunidades.update(list =>
            list.map(o => o.id === ultimaOp.id ? ultimaOp : o)
          );
        }

        // Limpar seleção e fechar
        this.candidatosSelecionados.set(new Set());
        this.finalizandoOportunidade.set(false);
        alert('Alunos aprovados com sucesso!');
        this.fecharListaCandidatos();
      },
      (err) => {
        console.error('Erro ao aprovar candidatos:', err);
        alert('Erro ao aprovar alguns candidatos.');
        this.finalizandoOportunidade.set(false);
      }
    );
  }}
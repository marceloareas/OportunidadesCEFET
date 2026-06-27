import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { PostComponent } from '../../components/post/post';
import { CandidatosModal } from '../../components/candidatos-modal/candidatos-modal';
import { OportunidadeService, Oportunidade } from '../../services/oportunidade.service';
import { FeedItem } from '../../services/feed.service';

@Component({
  selector: 'app-oportunidades',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NavbarTop, NavbarLeft, NavbarRight, PostComponent, CandidatosModal],
  templateUrl: './oportunidades.html',
  styleUrl: './oportunidades.css'
})
export class OportunidadesPage {

  paginaAtual = signal(0);
  tamanhoPagina = signal(10);
  totalPaginas = signal(0);

  usuarioId?: string;
  usuarioFuncao?: string;

  minhasOportunidades = signal<Oportunidade[]>([]);

  carregando = signal<boolean>(false);
  erro = signal<string>('');

  // modal candidatos
  mostrandoListaCandidatos = signal<boolean>(false);
  oportunidadeSelecionadaId = signal<string | null>(null);
  candidaturas = signal<Oportunidade[]>([]);

  constructor(
    private oportunidadeService: OportunidadeService,
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
      this.mostrandoListaCandidatos.set(true);
    }

  fecharListaCandidatos() {
    this.mostrandoListaCandidatos.set(false);
    this.oportunidadeSelecionadaId.set(null);
  }

  oportunidadeSelecionadaFinalizada(): boolean {
    const opId = this.oportunidadeSelecionadaId();
    if (!opId) return false;
    const op = this.minhasOportunidades().find(o => o.id === opId);
    return Boolean(op?.finalizada) || op?.status === 'FINALIZADA';
  }

  aoAprovarCandidatos(op: Oportunidade) {
    this.minhasOportunidades.update(list =>
      list.map(o => o.id === op.id ? op : o)
    );
  }

  statusAluno(op: Oportunidade): string {
    switch (op.statusCandidaturaAluno) {
      case 'APROVADO':
        return 'Aprovado';
      case 'RESERVA':
        return 'Reserva';
      case 'CONCORRENDO':
        return 'Concorrendo';
      default:
        return 'Pendente';
    }
  }

  corStatusAluno(op: Oportunidade): string {
    switch (op.statusCandidaturaAluno) {
      case 'APROVADO':
        return '#4CAF50';
      case 'RESERVA':
        return '#9e9e9e';
      case 'CONCORRENDO':
        return '#1976d2';
      default:
        return '#9e9e9e';
    }
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
      statusCandidaturaAluno: op.statusCandidaturaAluno,
    };
  }
}

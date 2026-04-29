import { Component, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { OportunidadeService, Oportunidade } from '../../services/oportunidade.service';
import { UsuarioService, Usuario } from '../../services/usuario.service';

@Component({
  selector: 'app-oportunidades',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarTop, NavbarLeft, NavbarRight],
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

  constructor(
    private oportunidadeService: OportunidadeService,
    private usuarioService: UsuarioService
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
      this.mostrandoListaCandidatos.set(true);

      this.carregarCandidatos(op.id);
    }

  fecharListaCandidatos() {
    this.mostrandoListaCandidatos.set(false);
    this.oportunidadeSelecionadaId.set(null);
    this.somenteAprovados.set(false);
  }

  carregarCandidatos(opId: string) {
    this.candidatosCarregando.set(true);
    this.candidatosErro.set('');

    this.oportunidadeService
      .listarCandidatos(opId, this.paginaAtualCandidatos(), this.tamanhoPagina())
      .subscribe({
        next: (page) => {

          this.candidatosPorOportunidade.update(m => ({
            ...m,
            [opId]: page.content
          }));

          this.totalPaginasCandidatos.set(page.totalPages);
          this.candidatosCarregando.set(false);
        },
        error: () => {
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
}

import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { OportunidadeService, Oportunidade } from '../../services/oportunidade.service';
import { UsuarioService, Usuario } from '../../services/usuario.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-oportunidades',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarTop, NavbarLeft, NavbarRight],
  templateUrl: './oportunidades.html',
  styleUrl: './oportunidades.css'
})
export class OportunidadesPage {
  usuarioId?: string;
  usuarioFuncao?: string;

  minhasOportunidades = signal<Oportunidade[]>([]);
  candidaturas = signal<Oportunidade[]>([]);
  candidatosPorOportunidade = signal<Record<string, Usuario[]>>({});

  carregando = signal<boolean>(false);
  erro = signal<string>('');

  // modal candidatos
  mostrandoListaCandidatos = signal<boolean>(false);
  oportunidadeSelecionadaId = signal<string | null>(null);
  somenteAprovados = signal<boolean>(false);
  aprovando = signal<string | null>(null);
  candidatosErro = signal<string>('');
  candidatosCarregando = signal<boolean>(false);

  constructor(
    private oportunidadeService: OportunidadeService,
    private usuarioService: UsuarioService
  ) {}

  ngOnInit() {
    const usuarioStr = localStorage.getItem('usuario');
    if (!usuarioStr) return;
    let parsed: any = null;
    try { parsed = JSON.parse(usuarioStr); } catch { return; }
    this.usuarioId = parsed?.id;
    this.usuarioFuncao = parsed?.funcao?.toLowerCase();
    if (!this.usuarioId) return;

    this.carregarDados();
  }

  private carregarDados() {
    this.carregando.set(true);
    this.erro.set('');

    this.oportunidadeService.listar().subscribe({
      next: (ops) => {
        const lista = ops || [];
        if (this.usuarioFuncao === 'professor') {
          this.minhasOportunidades.set(lista.filter(o => o.professorId === this.usuarioId));
        } else {
          // aluno: candidaturas
          this.candidaturas.set(
            lista.filter(o => (o.alunosCandidatosId || []).includes(this.usuarioId!))
          );
        }
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
    this.carregarCandidatos(op.id);
  }

  fecharListaCandidatos() {
    this.mostrandoListaCandidatos.set(false);
    this.oportunidadeSelecionadaId.set(null);
    this.somenteAprovados.set(false);
  }

  private carregarCandidatos(opId: string) {
    const ids = this.minhasOportunidades().find(o => o.id === opId)?.alunosCandidatosId || [];
    if (!ids.length) {
      this.candidatosPorOportunidade.update(m => ({ ...m, [opId]: [] }));
      return;
    }
    this.candidatosCarregando.set(true);
    this.candidatosErro.set('');
    const requests = ids.map(id => this.usuarioService.buscarPorId(id));
    forkJoin(requests).subscribe({
      next: (users) => {
        this.candidatosPorOportunidade.update(m => ({ ...m, [opId]: users.filter(Boolean) as Usuario[] }));
        this.candidatosCarregando.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar candidatos:', err);
        this.candidatosErro.set('Não foi possível carregar candidatos.');
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

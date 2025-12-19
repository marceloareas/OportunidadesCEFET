import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostService, Post } from '../../services/post.services';
import { OportunidadeService, Oportunidade } from '../../services/oportunidade.service';
import { UsuarioService, Usuario } from '../../services/usuario.service';
import { forkJoin } from 'rxjs';
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
  minhasOportunidades = signal<Oportunidade[]>([]);
  candidatosPorOportunidade = signal<Record<string, Array<{ id: string; nome?: string; aprovado?: boolean }>>>({});
  somenteAprovados = signal<boolean>(false);

  // modal de candidato
  candidatoSelecionado = signal<Usuario | null>(null);
  mostrandoCandidato = signal(false);
  // modal de lista de candidatos por oportunidade
  mostrandoListaCandidatos = signal(false);
  oportunidadeSelecionadaId = signal<string | null>(null);

  usuarioId?: string;
  usuarioFuncao?: string;

  constructor(
    private postService: PostService,
    private oportunidadeService: OportunidadeService,
    private usuarioService: UsuarioService
  ) {}

  aprovarCandidato(opId: string | null | undefined, alunoId: string | null | undefined) {
    if (!opId || !alunoId || !this.usuarioId) return;
    this.oportunidadeService.aprovarCandidatoDoProfessor(opId, alunoId, this.usuarioId).subscribe({
      next: (oportunidadeAtualizada) => {
        this.minhasOportunidades.update(list =>
          list.map(op => op.id === oportunidadeAtualizada.id ? oportunidadeAtualizada : op));

        // marca candidato como aprovado
        this.candidatosPorOportunidade.update(map => {
          const arr = (map[opId] || []).map(c => c.id === alunoId ? { ...c, aprovado: true } : c);
          return { ...map, [opId]: arr };
        });

        alert('Candidato aprovado com sucesso.');
      },
      error: (err) => {
        console.error('Erro ao aprovar candidato:', err);
        const mensagem = err?.error || err?.message || 'Erro ao aprovar candidato.';
        alert(mensagem);
      }
    });
  }

  finalizarOportunidade(opId: string | null | undefined) {
    if (!opId) return;
    if (!confirm('Deseja realmente finalizar esta oportunidade? Isso bloqueará novas candidaturas.')) return;
    this.oportunidadeService.finalizarOportunidade(opId).subscribe({
      next: (o) => {
        this.minhasOportunidades.update(list => list.map(op => op.id === o.id ? o : op));
        alert('Oportunidade finalizada com sucesso.');
      },
      error: (err) => {
        console.error('Erro ao finalizar oportunidade:', err);
        const mensagem = err?.error || err?.message || 'Erro ao finalizar oportunidade.';
        alert(mensagem);
      }
      
    });
  }

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

    // carregar posts do usuário
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

    // se for professor, carregar oportunidades e candidatos
    if (this.usuarioFuncao === 'professor') {
      this.oportunidadeService.listar().subscribe({
        next: (ops) => {
          const minhas = (ops || []).filter(o => o.professorId === this.usuarioId);
          this.minhasOportunidades.set(minhas);

          // buscar nomes dos candidatos para cada oportunidade
          for (const op of minhas) {
            const candidatos = op.alunosCandidatosId ?? [];
            const aprovados = new Set(op.alunosAprovadosId || []);
            if (!candidatos.length) {
              this.candidatosPorOportunidade.update(s => ({ ...s, [op.id!]: [] }));
              continue;
            }
            const requests = candidatos.map(id => this.usuarioService.buscarPorId(id));
            forkJoin(requests).subscribe({
              next: (users: Usuario[]) => {
                const list = (users || []).map(u => ({ id: u.id!, nome: u.nome, aprovado: aprovados.has(u.id!) }));
                this.candidatosPorOportunidade.update(s => ({ ...s, [op.id!]: list }));
              },
              error: (err) => {
                console.warn('Erro ao buscar candidatos:', err);
                this.candidatosPorOportunidade.update(s => ({
                  ...s,
                  [op.id!]: candidatos.map(id => ({ id, aprovado: aprovados.has(id) }))
                }));
              }
            });
          }
        },
        error: (err) => console.error('Erro ao carregar oportunidades:', err)
      });
    }
  }

  verCandidato(id: string | undefined) {
    if (!id) return;
    this.usuarioService.buscarPorId(id).subscribe({
      next: (u) => {
        this.candidatoSelecionado.set(u as Usuario);
        this.mostrandoCandidato.set(true);
      },
      error: (err) => {
        console.error('Erro ao buscar usuário:', err);
        alert('Erro ao carregar dados do candidato.');
      }
    });
  }

  abrirListaCandidatos(opId: string | undefined) {
    if (!opId) return;
    this.oportunidadeSelecionadaId.set(opId);
    this.mostrandoListaCandidatos.set(true);
  }

  fecharListaCandidatos() {
    this.mostrandoListaCandidatos.set(false);
    this.oportunidadeSelecionadaId.set(null);
  }

  fecharModalCandidato() {
    this.mostrandoCandidato.set(false);
    this.candidatoSelecionado.set(null);
  }

  estaAprovado(opId: string | null, candId: string | null | undefined): boolean {
    if (!opId || !candId) return false;
    const aprovados = this.minhasOportunidades().find(o => o.id === opId)?.alunosAprovadosId || [];
    return aprovados.includes(candId);
  }

  candidatosFiltrados(opId: string | null): Array<{ id: string; nome?: string; aprovado?: boolean; email?: string; matricula?: string; funcao?: string }> {
    if (!opId) return [];
    const arr = this.candidatosPorOportunidade()[opId] || [];
    if (!this.somenteAprovados()) return arr;
    return arr.filter(c => this.estaAprovado(opId, c.id) || c.aprovado);
  }
}

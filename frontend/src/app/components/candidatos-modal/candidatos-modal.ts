import { Component, EventEmitter, Input, Output, SimpleChanges, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import {
  OportunidadeService,
  Oportunidade,
  CandidatoComStatus,
} from '../../services/oportunidade.service';

@Component({
  selector: 'app-candidatos-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './candidatos-modal.html',
  styleUrl: './candidatos-modal.css',
})
export class CandidatosModal {
  @Input() oportunidadeId: string | null = null;
  @Input() professorId: string | null = null;
  @Input() finalizada = false;

  @Output() fechar = new EventEmitter<void>();
  @Output() aprovados = new EventEmitter<Oportunidade>();

  candidatos = signal<CandidatoComStatus[]>([]);
  candidatosCarregando = signal<boolean>(false);
  candidatosErro = signal<string>('');

  candidatosSelecionados = signal<Set<string>>(new Set());
  mostrandoModalConfirmacao = signal<boolean>(false);
  finalizandoOportunidade = signal<boolean>(false);

  constructor(private oportunidadeService: OportunidadeService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['oportunidadeId'] && this.oportunidadeId) {
      this.candidatosSelecionados.set(new Set());
      this.mostrandoModalConfirmacao.set(false);
      this.carregarCandidatos(this.oportunidadeId);
    }
  }

  private carregarCandidatos(opId: string) {
    if (!this.professorId) return;

    this.candidatosCarregando.set(true);
    this.candidatosErro.set('');

    this.oportunidadeService
      .listarCandidatosDoProfessor(opId, this.professorId)
      .subscribe({
        next: (lista: CandidatoComStatus[]) => {
          this.candidatos.set(lista || []);
          this.candidatosCarregando.set(false);
        },
        error: (err) => {
          console.error('Erro ao carregar candidatos:', err);
          this.candidatosErro.set('Erro ao carregar candidatos');
          this.candidatosCarregando.set(false);
        },
      });
  }

  candidatosFiltrados(): CandidatoComStatus[] {
    return this.candidatos();
  }

  estaAprovado(candidato: CandidatoComStatus): boolean {
    return candidato.status === 'APROVADO';
  }

  statusCandidaturaDescricao(status: CandidatoComStatus['status']): string {
    switch (status) {
      case 'APROVADO':
        return 'Aprovado';
      case 'RESERVA':
        return 'Reserva';
      default:
        return 'Concorrendo';
    }
  }

  fecharModal() {
    this.fechar.emit();
  }

  // Seleção em lote
  toggleCandidato(candId: string | undefined) {
    if (!candId || this.finalizada) return;
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
    if (this.finalizada) {
      alert('Esta oportunidade já está finalizada. Não é possível aprovar candidatos.');
      return;
    }
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
    const opId = this.oportunidadeId;
    if (!opId || !this.professorId) return;
    if (this.finalizada) {
      this.mostrandoModalConfirmacao.set(false);
      alert('Esta oportunidade já está finalizada. Não é possível aprovar candidatos.');
      return;
    }

    const selecionados = Array.from(this.candidatosSelecionados());
    if (selecionados.length === 0) return;

    this.finalizandoOportunidade.set(true);
    this.mostrandoModalConfirmacao.set(false);

    const aprovacoes = selecionados.map((idAluno) =>
      this.oportunidadeService.aprovarCandidatoDoProfessor(opId, idAluno, this.professorId!)
    );

    Promise.all(aprovacoes.map((obs) => obs.toPromise())).then(
      (resultados: any[]) => {
        const ultimaOp = resultados.find((r) => r && r.id);
        if (ultimaOp) {
          this.aprovados.emit(ultimaOp);
        }

        this.candidatosSelecionados.set(new Set());
        this.finalizandoOportunidade.set(false);
        alert('Alunos aprovados com sucesso!');
        this.fecharModal();
      },
      (err) => {
        console.error('Erro ao aprovar candidatos:', err);
        alert('Erro ao aprovar alguns candidatos.');
        this.finalizandoOportunidade.set(false);
      }
    );
  }
}

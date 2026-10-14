import { Component, EventEmitter, Input, Output, SimpleChanges, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import {
  OportunidadeService,
  Oportunidade,
  CandidatoComStatus,
} from '../../services/oportunidade.service';
import { FeedbackService } from '../../services/feedback.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';

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
  finalizandoOportunidade = signal<boolean>(false);

  private feedback = inject(FeedbackService);
  private confirmDialog = inject(ConfirmDialogService);

  constructor(private oportunidadeService: OportunidadeService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['oportunidadeId'] && this.oportunidadeId) {
      this.candidatosSelecionados.set(new Set());
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
      this.feedback.error('Esta oportunidade já está finalizada. Não é possível aprovar candidatos.');
      return;
    }
    if (this.candidatosSelecionados().size === 0) {
      this.feedback.error('Selecione pelo menos um aluno para finalizar.');
      return;
    }

    const quantidade = this.candidatosSelecionados().size;

    this.confirmDialog
      .confirm({
        title: 'Aprovar candidatos',
        message: `Deseja aprovar ${quantidade} aluno(s) na oportunidade? Essa ação alterará o status de candidatura destes alunos para aprovado.`,
        confirmLabel: 'Confirmar',
      })
      .subscribe((confirmado) => {
        if (confirmado) this.confirmarFinalizacao();
      });
  }

  confirmarFinalizacao() {
    const opId = this.oportunidadeId;
    if (!opId || !this.professorId) return;
    if (this.finalizada) {
      this.feedback.error('Esta oportunidade já está finalizada. Não é possível aprovar candidatos.');
      return;
    }

    const selecionados = Array.from(this.candidatosSelecionados());
    if (selecionados.length === 0) return;

    this.finalizandoOportunidade.set(true);

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
        this.feedback.success('Alunos aprovados com sucesso!');
        this.fecharModal();
      },
      (err) => {
        console.error('Erro ao aprovar candidatos:', err);
        this.feedback.error('Erro ao aprovar alguns candidatos.');
        this.finalizandoOportunidade.set(false);
      }
    );
  }
}

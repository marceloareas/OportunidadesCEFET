import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SavedItemsService } from '../../services/itens-salvos.service';
import { FeedItem } from '../../services/feed.service';
import { Oportunidade } from '../../services/oportunidade.service';

import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { PostComponent } from '../../components/post/post';
import { CandidatosModal } from '../../components/candidatos-modal/candidatos-modal';

import { Router } from '@angular/router';

@Component({
  selector: 'app-itens-salvos',
  standalone: true,
  imports: [CommonModule, NavbarTop, NavbarLeft, NavbarRight, PostComponent, CandidatosModal],
  templateUrl: './itens-salvos.html',
  styleUrls: ['./itens-salvos.css'],
})
export class ItensSalvosComponent implements OnInit {
  itens: FeedItem[] = [];
  carregando = false;
  erro: string | null = null;

  userId: string = '';

  // modal de candidatos (professor vendo candidatos de uma oportunidade salva)
  mostrandoModalCandidatos = signal<boolean>(false);
  oportunidadeSelecionadaId = signal<string | null>(null);
  professorSelecionadoId = signal<string | null>(null);
  oportunidadeSelecionadaFinalizada = signal<boolean>(false);

  constructor(
    private savedService: SavedItemsService,
    private router: Router,
  ) {}

  trackById(index: number, item: FeedItem) {
    return item.id;
  }

  ngOnInit() {
    if (typeof window === 'undefined') {
      return;
    }

    const stored = localStorage.getItem('usuario');

    if (!stored) {
      this.erro = 'Usuário não encontrado';
      return;
    }

    try {
      const u = JSON.parse(stored) as any;
      this.userId = u.id?.toString();

      if (!this.userId) {
        this.erro = 'ID do usuário inválido';
        return;
      }

      this.buscarItensSalvos();
    } catch (e) {
      console.error('Erro ao parsear usuário', e);
      this.erro = 'Erro ao carregar usuário';
    }
  }

  abrirModalCandidatos(item: FeedItem) {
    const refId = item.referenciaId || item.id;
    if (!refId) return;

    this.oportunidadeSelecionadaId.set(refId);
    this.professorSelecionadoId.set(item.criadorId ?? null);
    this.oportunidadeSelecionadaFinalizada.set(
      Boolean(item.finalizada) || item.status === 'FINALIZADA'
    );
    this.mostrandoModalCandidatos.set(true);
  }

  fecharModalCandidatos() {
    this.mostrandoModalCandidatos.set(false);
    this.oportunidadeSelecionadaId.set(null);
    this.professorSelecionadoId.set(null);
    this.oportunidadeSelecionadaFinalizada.set(false);
  }

  aoAprovarCandidatos(oportunidade: Oportunidade) {
    this.itens = this.itens.map(item => {
      const refId = item.referenciaId || item.id;
      return refId === oportunidade.id
        ? {
            ...item,
            vagasPreenchidas: oportunidade.vagasPreenchidas,
            finalizada: oportunidade.finalizada,
            status: oportunidade.status,
            alunosAprovadosId: oportunidade.alunosAprovadosId,
            alunosCandidatosId: oportunidade.alunosCandidatosId,
          }
        : item;
    });
  }

  buscarItensSalvos() {
    this.carregando = true;

    this.savedService.listarPorUsuario(this.userId).subscribe({
      next: (res) => {
        this.itens = res.content || [];
        this.carregando = false;
      },
      error: (err) => {
        console.error(err);
        this.erro = 'Erro ao carregar itens salvos';
        this.carregando = false;
      },
    });
  }
}

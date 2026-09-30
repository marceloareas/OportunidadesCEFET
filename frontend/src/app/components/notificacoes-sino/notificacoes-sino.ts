import { Component, OnDestroy, OnInit, PLATFORM_ID, inject, signal } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { MatMenuModule } from '@angular/material/menu';
import { Subscription, catchError, of, switchMap, timer } from 'rxjs';
import { Notificacao, NotificacaoService } from '../../services/notificacao.service';
import { FeedbackService } from '../../services/feedback.service';
import { iconePorTipo, rotaPorTipo, rotuloContagem, tempoRelativo } from '../../utils/notificacao';

const INTERVALO_CONTAGEM_MS = 30000;

@Component({
  selector: 'app-notificacoes-sino',
  standalone: true,
  imports: [CommonModule, MatMenuModule],
  templateUrl: './notificacoes-sino.html',
  styleUrl: './notificacoes-sino.css',
})
export class NotificacoesSino implements OnInit, OnDestroy {
  private notificacaoService = inject(NotificacaoService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);
  private feedback = inject(FeedbackService);
  private polling?: Subscription;

  ativo = signal(false);
  naoLidas = signal(0);
  notificacoes = signal<Notificacao[]>([]);
  carregando = signal(false);

  readonly rotuloContagem = rotuloContagem;
  readonly tempoRelativo = tempoRelativo;
  readonly iconePorTipo = iconePorTipo;

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId) || !localStorage.getItem('token')) {
      return;
    }
    this.ativo.set(true);
    this.polling = timer(0, INTERVALO_CONTAGEM_MS)
      .pipe(
        switchMap(() =>
          this.notificacaoService.contarNaoLidas().pipe(catchError(() => of(null)))
        )
      )
      .subscribe((total) => {
        if (total !== null) {
          this.naoLidas.set(total);
        }
      });
  }

  ngOnDestroy(): void {
    this.polling?.unsubscribe();
  }

  carregarLista(): void {
    this.carregando.set(true);
    this.notificacaoService.listar().subscribe({
      next: (lista) => {
        this.notificacoes.set(lista);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }

  abrir(notificacao: Notificacao): void {
    const navegar = () => this.router.navigate([rotaPorTipo(notificacao.tipo)]);
    if (notificacao.lida) {
      navegar();
      return;
    }
    this.notificacaoService.marcarComoLida(notificacao.id).subscribe({
      next: () => {
        this.notificacoes.update((lista) =>
          lista.map((n) => (n.id === notificacao.id ? { ...n, lida: true } : n))
        );
        this.naoLidas.update((total) => Math.max(0, total - 1));
        navegar();
      },
      error: () => navegar(),
    });
  }

  marcarTodas(): void {
    this.notificacaoService.marcarTodasComoLidas().subscribe({
      next: () => {
        this.notificacoes.update((lista) => lista.map((n) => ({ ...n, lida: true })));
        this.naoLidas.set(0);
      },
      error: () => this.feedback.error('Não foi possível marcar as notificações como lidas.'),
    });
  }

  temNaoLidas(): boolean {
    return this.notificacoes().some((n) => !n.lida);
  }
}

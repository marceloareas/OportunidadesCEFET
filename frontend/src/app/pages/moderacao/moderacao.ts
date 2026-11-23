import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';

import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { AdminService } from '../../services/admin.service';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';
import { FeedbackService } from '../../services/feedback.service';
import { Usuario } from '../../services/usuario.service';
import { avatarOuPadrao } from '../../utils/avatar';

const ROTULO_PERFIL: Record<string, string> = {
  ALUNO: 'Aluno',
  PROFESSOR: 'Professor',
  ADMIN: 'Administrador',
};

// Sprint 6: o administrador lista os usuários e desativa ou reativa contas.
@Component({
  selector: 'app-moderacao',
  standalone: true,
  imports: [RouterLink, NavbarTop, NavbarLeft, NavbarRight],
  templateUrl: './moderacao.html',
  styleUrl: './moderacao.css',
})
export class Moderacao implements OnInit {
  readonly tamanhoPagina = 10;
  readonly avatarOuPadrao = avatarOuPadrao;

  private admin = inject(AdminService);
  private confirmDialog = inject(ConfirmDialogService);
  private feedback = inject(FeedbackService);

  usuarios = signal<Usuario[]>([]);
  termo = signal('');
  // Termo da última busca enviada: a paginação e o aviso de vazio usam este, não o que está digitado.
  termoAplicado = signal('');
  private pedidoAtual?: Subscription;
  pagina = signal(0);
  totalPaginas = signal(0);
  totalUsuarios = signal(0);
  carregando = signal(false);
  erro = signal<string | null>(null);
  processando = signal<string | null>(null);

  ngOnInit(): void {
    this.carregar(0);
  }

  carregar(pagina: number): void {
    // Uma resposta atrasada de um pedido anterior não pode sobrescrever a lista mais nova.
    this.pedidoAtual?.unsubscribe();
    this.carregando.set(true);
    this.erro.set(null);
    this.pedidoAtual = this.admin.listarUsuarios(this.termoAplicado(), pagina, this.tamanhoPagina).subscribe({
      next: (resultado) => {
        this.usuarios.set(resultado.content);
        this.pagina.set(resultado.number ?? pagina);
        this.totalPaginas.set(resultado.totalPages);
        this.totalUsuarios.set(resultado.totalElements);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar os usuários. Tente de novo em instantes.');
      },
    });
  }

  buscar(evento: Event): void {
    evento.preventDefault();
    this.termoAplicado.set(this.termo().trim());
    this.carregar(0);
  }

  limparBusca(): void {
    this.termo.set('');
    this.termoAplicado.set('');
    this.carregar(0);
  }

  rotuloPerfil(funcao?: string): string {
    return ROTULO_PERFIL[funcao?.toUpperCase() ?? ''] ?? 'Usuário';
  }

  ehAdmin(usuario: Usuario): boolean {
    return usuario.funcao?.toUpperCase() === 'ADMIN';
  }

  ativo(usuario: Usuario): boolean {
    return usuario.ativo !== false;
  }

  alternar(usuario: Usuario): void {
    if (!usuario.id || this.processando()) return;
    if (!this.ativo(usuario)) {
      this.executar(usuario, 'reativar');
      return;
    }
    this.confirmDialog
      .confirm({
        title: 'Desativar usuário',
        message: `${usuario.nome} não vai mais conseguir entrar no sistema. As publicações continuam no ar e você pode reativar a conta depois.`,
        confirmLabel: 'Desativar',
      })
      .subscribe((confirmado) => {
        if (confirmado) this.executar(usuario, 'desativar');
      });
  }

  private executar(usuario: Usuario, acao: 'desativar' | 'reativar'): void {
    const id = usuario.id!;
    this.processando.set(id);
    const pedido = acao === 'desativar' ? this.admin.desativar(id) : this.admin.reativar(id);
    pedido.subscribe({
      next: (atualizado) => {
        this.usuarios.update((lista) => lista.map((u) => (u.id === id ? { ...u, ...atualizado } : u)));
        this.processando.set(null);
        this.feedback.success(
          acao === 'desativar' ? `A conta de ${usuario.nome} foi desativada.` : `A conta de ${usuario.nome} foi reativada.`,
        );
      },
      error: (err) => {
        this.processando.set(null);
        this.feedback.error(
          err?.error?.message || (acao === 'desativar' ? 'Não foi possível desativar a conta.' : 'Não foi possível reativar a conta.'),
        );
      },
    });
  }
}

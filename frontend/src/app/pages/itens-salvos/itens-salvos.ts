import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SavedItemsService } from '../../services/itens-salvos.service';
import { FeedItem } from '../../services/feed.service';

import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { PostComponent } from '../../components/post/post';

import { Router } from '@angular/router';

@Component({
  selector: 'app-itens-salvos',
  standalone: true,
  imports: [CommonModule, NavbarTop, NavbarLeft, NavbarRight, PostComponent],
  templateUrl: './itens-salvos.html',
  styleUrls: ['./itens-salvos.css'],
})
export class ItensSalvosComponent implements OnInit {
  itens: FeedItem[] = [];
  carregando = false;
  erro: string | null = null;

  userId: string = '';

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

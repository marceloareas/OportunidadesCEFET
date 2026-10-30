import { Injectable, signal } from '@angular/core';

export type Tema = 'claro' | 'escuro';

export const CHAVE_TEMA = 'tema';

// Tema claro/escuro (B4). A escolha fica salva no navegador; sem escolha, segue o sistema.
// O index.html aplica o mesmo atributo antes do Angular subir, para a tela não piscar.
@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly tema = signal<Tema>(this.temaInicial());

  constructor() {
    this.aplicar(this.tema());
  }

  alternar(): void {
    const proximo: Tema = this.tema() === 'escuro' ? 'claro' : 'escuro';
    this.tema.set(proximo);
    this.aplicar(proximo);
    try {
      localStorage.setItem(CHAVE_TEMA, proximo);
    } catch {
      // Sem localStorage (modo privado restrito): o tema vale só nesta aba.
    }
  }

  private temaInicial(): Tema {
    if (typeof window === 'undefined') return 'claro';
    try {
      const salvo = localStorage.getItem(CHAVE_TEMA);
      if (salvo === 'claro' || salvo === 'escuro') return salvo;
    } catch {
      // segue para a preferência do sistema
    }
    return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'escuro' : 'claro';
  }

  private aplicar(tema: Tema): void {
    if (typeof document === 'undefined') return;
    document.documentElement.setAttribute('data-theme', tema === 'escuro' ? 'dark' : 'light');
  }
}

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

function estaLogado(): boolean {
  return typeof window !== 'undefined' && Boolean(localStorage.getItem('token') && localStorage.getItem('usuario'));
}

// Páginas internas: sem sessão, volta para o login em vez de mostrar uma tela vazia cheia de erros.
export const authGuard: CanActivateFn = () =>
  estaLogado() ? true : inject(Router).createUrlTree(['/login']);

// Tela de login: quem já está logado vai direto para o feed.
export const visitanteGuard: CanActivateFn = () =>
  estaLogado() ? inject(Router).createUrlTree(['/home']) : true;

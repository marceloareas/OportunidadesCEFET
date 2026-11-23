import { TestBed } from '@angular/core/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';

import { adminGuard, authGuard, visitanteGuard } from './auth.guard';
import { deslogar, logarComo } from '../../testing/test-providers';

describe('guardas de rota', () => {
  const rodar = (guarda: typeof authGuard) =>
    TestBed.runInInjectionContext(() => guarda({} as any, {} as any));

  beforeEach(() => TestBed.configureTestingModule({ providers: [provideRouter([])] }));
  afterEach(() => deslogar());

  it('sem sessão, página interna manda para o login', () => {
    const resultado = rodar(authGuard) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/login');
  });

  it('com sessão, página interna abre', () => {
    logarComo({ id: 'u1', nome: 'Bia', funcao: 'ALUNO' });
    expect(rodar(authGuard)).toBeTrue();
  });

  it('logado não vê a tela de login de novo', () => {
    logarComo({ id: 'u1', nome: 'Bia', funcao: 'ALUNO' });
    const resultado = rodar(visitanteGuard) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/home');
  });

  it('visitante vê a tela de login', () => {
    expect(rodar(visitanteGuard)).toBeTrue();
  });

  it('administrador abre a moderação', () => {
    logarComo({ id: 'adm', nome: 'Administrador', funcao: 'ADMIN' });
    expect(rodar(adminGuard)).toBeTrue();
  });

  it('aluno que tenta abrir a moderação volta para o feed', () => {
    logarComo({ id: 'u1', nome: 'Bia', funcao: 'ALUNO' });
    const resultado = rodar(adminGuard) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/home');
  });
});

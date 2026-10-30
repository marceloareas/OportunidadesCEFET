import { TestBed } from '@angular/core/testing';
import { CHAVE_TEMA, ThemeService } from './theme.service';

describe('ThemeService (B4)', () => {
  const atributo = () => document.documentElement.getAttribute('data-theme');

  function criar(preferenciaEscura: boolean): ThemeService {
    spyOn(window, 'matchMedia').and.returnValue({ matches: preferenciaEscura } as MediaQueryList);
    TestBed.configureTestingModule({});
    return TestBed.inject(ThemeService);
  }

  afterEach(() => {
    localStorage.removeItem(CHAVE_TEMA);
    document.documentElement.removeAttribute('data-theme');
  });

  it('sem escolha salva, segue o tema do sistema', () => {
    const tema = criar(true);
    expect(tema.tema()).toBe('escuro');
    expect(atributo()).toBe('dark');
  });

  it('a escolha salva vence a preferência do sistema', () => {
    localStorage.setItem(CHAVE_TEMA, 'claro');
    const tema = criar(true);
    expect(tema.tema()).toBe('claro');
    expect(atributo()).toBe('light');
  });

  it('alternar troca o atributo e guarda a escolha', () => {
    const tema = criar(false);

    tema.alternar();
    expect(tema.tema()).toBe('escuro');
    expect(atributo()).toBe('dark');
    expect(localStorage.getItem(CHAVE_TEMA)).toBe('escuro');

    tema.alternar();
    expect(atributo()).toBe('light');
    expect(localStorage.getItem(CHAVE_TEMA)).toBe('claro');
  });

  it('o logout não apaga a preferência de tema', async () => {
    const { CHAVES_SESSAO } = await import('./auth.service');
    expect(CHAVES_SESSAO).not.toContain(CHAVE_TEMA);
  });
});

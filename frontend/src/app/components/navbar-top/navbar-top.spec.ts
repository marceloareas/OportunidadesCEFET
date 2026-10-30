import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { NavbarTop } from './navbar-top';
import { CHAVE_TEMA } from '../../services/theme.service';

describe('NavbarTop', () => {
  let component: NavbarTop;
  let fixture: ComponentFixture<NavbarTop>;

  beforeEach(async () => {
    localStorage.setItem(CHAVE_TEMA, 'claro');
    await TestBed.configureTestingModule({
      imports: [NavbarTop],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavbarTop);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.removeItem(CHAVE_TEMA);
    document.documentElement.removeAttribute('data-theme');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('botão sol/lua alterna o tema e descreve a próxima ação (B4)', () => {
    const botao = fixture.nativeElement.querySelector('.btn-tema') as HTMLButtonElement;
    expect(botao.getAttribute('aria-label')).toBe('Usar tema escuro');

    botao.click();
    fixture.detectChanges();

    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
    expect(botao.getAttribute('aria-label')).toBe('Usar tema claro');
    expect(botao.querySelector('i')!.classList).toContain('bi-sun');
  });
});

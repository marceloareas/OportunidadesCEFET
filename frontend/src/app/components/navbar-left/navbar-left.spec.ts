import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarLeft } from './navbar-left';
import { deslogar, logarComo, providersDeTeste } from '../../../testing/test-providers';

describe('NavbarLeft', () => {
  let component: NavbarLeft;
  let fixture: ComponentFixture<NavbarLeft>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarLeft],
      providers: providersDeTeste()
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavbarLeft);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('aluno não vê o link de Moderação', () => {
    expect(fixture.nativeElement.querySelector('a[href="/moderacao"]')).toBeNull();
  });
});

describe('NavbarLeft como administrador', () => {
  beforeEach(async () => {
    logarComo({ id: 'adm', nome: 'Administrador', funcao: 'ADMIN' });
    await TestBed.configureTestingModule({ imports: [NavbarLeft], providers: providersDeTeste() }).compileComponents();
  });
  afterEach(() => deslogar());

  it('mostra o link de Moderação e esconde o de Oportunidades', () => {
    const fixture = TestBed.createComponent(NavbarLeft);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('a[href="/moderacao"]')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('a[href="/oportunidades"]')).toBeNull();
  });
});

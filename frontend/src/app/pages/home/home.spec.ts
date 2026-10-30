import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of } from 'rxjs';

import { Home } from './home';
import { FeedService } from '../../services/feed.service';
import { deslogar, logarComo, providersDeTeste } from '../../../testing/test-providers';

describe('Home', () => {
  let component: Home;
  let fixture: ComponentFixture<Home>;
  let feed: jasmine.SpyObj<FeedService>;

  beforeEach(async () => {
    logarComo({ id: 'aluno-1', nome: 'Bia', funcao: 'ALUNO' });
    feed = jasmine.createSpyObj<FeedService>('FeedService', ['listar']);
    feed.listar.and.returnValue(of({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 }));

    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [...providersDeTeste(), { provide: FeedService, useValue: feed }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => deslogar());

  const ultimosFiltros = () => feed.listar.calls.mostRecent().args[2];

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('busca por texto espera a digitação parar e manda o q (B1)', fakeAsync(() => {
    const chamadasAntes = feed.listar.calls.count();

    component.buscarTexto('rob');
    component.buscarTexto('robótica');
    tick(399);
    expect(feed.listar.calls.count()).toBe(chamadasAntes);

    tick(1);
    expect(feed.listar.calls.count()).toBe(chamadasAntes + 1);
    expect(ultimosFiltros()?.q).toBe('robótica');
    expect(component.filtrosAtivos()).toBeTrue();
  }));

  it('limpar filtros zera a busca e cancela a busca pendente', fakeAsync(() => {
    component.buscarTexto('robótica');
    component.limparFiltros();
    const chamadas = feed.listar.calls.count();

    tick(500);

    expect(feed.listar.calls.count()).toBe(chamadas);
    expect(component.filtroTexto()).toBe('');
    expect(ultimosFiltros()?.q).toBeUndefined();
    expect(component.filtrosAtivos()).toBeFalse();
  }));
});

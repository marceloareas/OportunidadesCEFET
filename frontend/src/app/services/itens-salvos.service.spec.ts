import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LIMITE_ITENS_SALVOS, SavedItemsService } from './itens-salvos.service';
import { API_BASE_URL } from '../config/app-env';

describe('SavedItemsService', () => {
  let service: SavedItemsService;
  let http: HttpTestingController;
  const API = `${API_BASE_URL}/saved-items`;
  const LISTA = `${API}/user/u1?page=0&size=${LIMITE_ITENS_SALVOS}`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SavedItemsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('vários posts perguntando "estou salvo?" geram uma única requisição', () => {
    const respostas: boolean[] = [];
    service.estaSalvo('u1', 'op-1').subscribe((s) => respostas.push(s));
    service.estaSalvo('u1', 'op-2').subscribe((s) => respostas.push(s));
    service.estaSalvo('u1', 'p-9').subscribe((s) => respostas.push(s));

    http.expectOne(LISTA).flush({ content: [{ referenciaId: 'op-1' }, { id: 'p-9' }] });

    expect(respostas).toEqual([true, false, true]);
  });

  it('salvar invalida o cache: a próxima pergunta busca de novo', () => {
    service.estaSalvo('u1', 'op-1').subscribe();
    http.expectOne(LISTA).flush({ content: [] });

    service.salvar('u1', 'op-1').subscribe();
    http.expectOne(API).flush(null);

    let salvo = false;
    service.estaSalvo('u1', 'op-1').subscribe((s) => (salvo = s));
    http.expectOne(LISTA).flush({ content: [{ referenciaId: 'op-1' }] });
    expect(salvo).toBeTrue();
  });

  it('erro ao buscar responde "não salvo" sem quebrar o post', () => {
    let salvo: boolean | undefined;
    service.estaSalvo('u1', 'op-1').subscribe((s) => (salvo = s));

    http.expectOne(LISTA).flush(null, { status: 500, statusText: 'Erro' });

    expect(salvo).toBeFalse();
  });
});

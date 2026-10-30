import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { OportunidadeService } from './oportunidade.service';
import { API_BASE_URL } from '../config/app-env';

describe('OportunidadeService', () => {
  let service: OportunidadeService;
  let http: HttpTestingController;
  const API = `${API_BASE_URL}/oportunidades`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(OportunidadeService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('criar() faz POST com a oportunidade no corpo', () => {
    service.criar({ nome: 'Monitoria', quantidadeDeVagas: 2 }).subscribe();
    const req = http.expectOne(API);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.nome).toBe('Monitoria');
    req.flush({ id: 'op-1', nome: 'Monitoria' });
  });

  it('candidatarAluno() faz POST em /{id}/candidatar/{aluno}', () => {
    service.candidatarAluno('op-1', 'aluno-1').subscribe();
    const req = http.expectOne(`${API}/op-1/candidatar/aluno-1`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 'op-1' });
  });

  it('aprovarCandidatoDoProfessor() faz POST com o professor na rota', () => {
    service.aprovarCandidatoDoProfessor('op-1', 'aluno-1', 'prof-1').subscribe();
    const req = http.expectOne(`${API}/op-1/aprovar/aluno-1/professor/prof-1`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 'op-1' });
  });

  it('finalizarOportunidade() faz POST em /{id}/finalizar', () => {
    let finalizada = false;
    service.finalizarOportunidade('op-1').subscribe((op) => (finalizada = Boolean(op.finalizada)));
    const req = http.expectOne(`${API}/op-1/finalizar`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 'op-1', nome: 'X', finalizada: true });
    expect(finalizada).toBeTrue();
  });

  it('listarPorAluno() envia a paginação', () => {
    service.listarPorAluno('aluno-1', 2, 5).subscribe();
    const req = http.expectOne(`${API}/aluno/aluno-1?page=2&size=5`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [], number: 2, totalPages: 0 });
  });

  it('listarCandidatosDoProfessor() devolve aluno + status', () => {
    let status = '';
    service.listarCandidatosDoProfessor('op-1', 'prof-1').subscribe((lista) => (status = lista[0].status));
    http
      .expectOne(`${API}/op-1/candidatos/professor/prof-1`)
      .flush([{ aluno: { id: 'aluno-1', nome: 'Bia' }, status: 'RESERVA' }]);
    expect(status).toBe('RESERVA');
  });
});

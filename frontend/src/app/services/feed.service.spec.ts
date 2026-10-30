import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { FeedService } from './feed.service';
import { API_BASE_URL } from '../config/app-env';

describe('FeedService', () => {
  let service: FeedService;
  let http: HttpTestingController;
  const API = `${API_BASE_URL}/feed`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(FeedService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sem filtros manda só a paginação', () => {
    service.listar(0, 10).subscribe();
    const req = http.expectOne((r) => r.url === API);
    expect(req.request.params.keys().sort()).toEqual(['page', 'size']);
    req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });

  it('envia a busca por texto (B1) junto com os filtros, sem espaços nas pontas', () => {
    service.listar(0, 10, { q: '  robótica ', categoria: 'PESQUISA' }).subscribe();
    const req = http.expectOne((r) => r.url === API);
    expect(req.request.params.get('q')).toBe('robótica');
    expect(req.request.params.get('categoria')).toBe('PESQUISA');
    req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });

  it('busca só com espaços não vira filtro', () => {
    service.listar(0, 10, { q: '   ' }).subscribe();
    const req = http.expectOne((r) => r.url === API);
    expect(req.request.params.has('q')).toBeFalse();
    req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });
});

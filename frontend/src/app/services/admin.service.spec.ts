import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { API_BASE_URL } from '../config/app-env';

describe('AdminService', () => {
  let service: AdminService;
  let http: HttpTestingController;
  const API = `${API_BASE_URL}/admin/usuarios`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AdminService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('listarUsuarios() pagina e manda a busca', () => {
    service.listarUsuarios('bia', 2, 10).subscribe();
    const req = http.expectOne((r) => r.url === API);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('busca')).toBe('bia');
    req.flush({ content: [], totalPages: 0, totalElements: 0, number: 2, size: 10 });
  });

  it('listarUsuarios() sem busca não manda o parâmetro', () => {
    service.listarUsuarios('', 0, 10).subscribe();
    const req = http.expectOne((r) => r.url === API);
    expect(req.request.params.has('busca')).toBeFalse();
    req.flush({ content: [], totalPages: 0, totalElements: 0, number: 0, size: 10 });
  });

  it('desativar() e reativar() fazem PATCH', () => {
    service.desativar('u1').subscribe();
    const des = http.expectOne(`${API}/u1/desativar`);
    expect(des.request.method).toBe('PATCH');
    des.flush({ id: 'u1', ativo: false });

    service.reativar('u1').subscribe();
    const rea = http.expectOne(`${API}/u1/reativar`);
    expect(rea.request.method).toBe('PATCH');
    rea.flush({ id: 'u1', ativo: true });
  });
});

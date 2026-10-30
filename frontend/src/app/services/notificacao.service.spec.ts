import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { NotificacaoService } from './notificacao.service';
import { API_BASE_URL } from '../config/app-env';

describe('NotificacaoService', () => {
  let service: NotificacaoService;
  let http: HttpTestingController;
  const API = `${API_BASE_URL}/notificacoes`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(NotificacaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('listar() faz GET em /notificacoes', () => {
    let quantidade = 0;
    service.listar().subscribe((lista) => (quantidade = lista.length));
    const req = http.expectOne(API);
    expect(req.request.method).toBe('GET');
    req.flush([
      { id: 'n1', usuarioId: 'u1', tipo: 'COMENTARIO_RECEBIDO', mensagem: 'Oi', lida: false, criadoEm: '2026-09-30T12:00:00Z' },
    ]);
    expect(quantidade).toBe(1);
  });

  it('contarNaoLidas() devolve o total', () => {
    let total = -1;
    service.contarNaoLidas().subscribe((t) => (total = t));
    http.expectOne(`${API}/nao-lidas/contagem`).flush({ total: 3 });
    expect(total).toBe(3);
  });

  it('marcarComoLida() faz PATCH em /{id}/lida', () => {
    service.marcarComoLida('n1').subscribe();
    const req = http.expectOne(`${API}/n1/lida`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });

  it('marcarTodasComoLidas() faz PATCH em /lidas', () => {
    service.marcarTodasComoLidas().subscribe();
    const req = http.expectOne(`${API}/lidas`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });
});

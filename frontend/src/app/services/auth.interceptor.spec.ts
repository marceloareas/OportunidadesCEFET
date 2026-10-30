import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { authInterceptor } from './auth.interceptor';
import { API_BASE_URL } from '../config/app-env';
import { deslogar } from '../../testing/test-providers';

describe('authInterceptor (B5)', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let router: Router;
  const FEED = `${API_BASE_URL}/feed`;
  const REFRESH = `${API_BASE_URL}/auth/refresh`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    localStorage.setItem('token', 'acesso-velho');
    localStorage.setItem('refreshToken', 'refresh-1');
    localStorage.setItem('usuario', '{"id":"u1"}');
  });

  afterEach(() => {
    backend.verify();
    deslogar();
  });

  it('envia o token no cabeçalho Authorization', () => {
    http.get(FEED).subscribe();

    const req = backend.expectOne(FEED);
    expect(req.request.headers.get('Authorization')).toBe('Bearer acesso-velho');
    req.flush({});
  });

  it('com 401 renova o token e repete a requisição', () => {
    let resposta: unknown;
    http.get(FEED).subscribe((r) => (resposta = r));

    backend.expectOne(FEED).flush(null, { status: 401, statusText: 'Unauthorized' });

    const refresh = backend.expectOne(REFRESH);
    expect(refresh.request.body).toEqual({ refreshToken: 'refresh-1' });
    refresh.flush({ token: 'acesso-novo', refreshToken: 'refresh-2', usuario: { id: 'u1' } });

    const repetida = backend.expectOne(FEED);
    expect(repetida.request.headers.get('Authorization')).toBe('Bearer acesso-novo');
    repetida.flush({ ok: true });

    expect(resposta).toEqual({ ok: true });
    expect(localStorage.getItem('token')).toBe('acesso-novo');
    expect(localStorage.getItem('refreshToken')).toBe('refresh-2');
  });

  it('duas requisições com 401 juntas fazem um único refresh', () => {
    http.get(FEED).subscribe();
    http.get(`${FEED}?page=1`).subscribe();

    backend.expectOne(FEED).flush(null, { status: 401, statusText: 'Unauthorized' });
    backend.expectOne(`${FEED}?page=1`).flush(null, { status: 401, statusText: 'Unauthorized' });

    backend.expectOne(REFRESH).flush({ token: 'acesso-novo', refreshToken: 'refresh-2', usuario: { id: 'u1' } });

    backend.expectOne(FEED).flush({});
    backend.expectOne(`${FEED}?page=1`).flush({});
  });

  it('se o refresh falha, encerra a sessão e volta ao login', () => {
    let falhou = false;
    http.get(FEED).subscribe({ error: () => (falhou = true) });

    backend.expectOne(FEED).flush(null, { status: 401, statusText: 'Unauthorized' });
    backend.expectOne(REFRESH).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(falhou).toBeTrue();
    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('usuario')).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('403 (sem permissão) não tenta renovar o token', () => {
    let status = 0;
    http.get(FEED).subscribe({ error: (e) => (status = e.status) });

    backend.expectOne(FEED).flush({ message: 'Sem permissão' }, { status: 403, statusText: 'Forbidden' });

    backend.expectNone(REFRESH);
    expect(status).toBe(403);
  });

  it('login errado (401) não tenta renovar', () => {
    const LOGIN = `${API_BASE_URL}/auth/login`;
    http.post(LOGIN, {}).subscribe({ error: () => undefined });

    backend.expectOne(LOGIN).flush(null, { status: 401, statusText: 'Unauthorized' });

    backend.expectNone(REFRESH);
  });

  it('sem refresh token guardado, o 401 segue como erro', () => {
    localStorage.removeItem('refreshToken');
    let status = 0;
    http.get(FEED).subscribe({ error: (e) => (status = e.status) });

    backend.expectOne(FEED).flush(null, { status: 401, statusText: 'Unauthorized' });

    backend.expectNone(REFRESH);
    expect(status).toBe(401);
  });
});

import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, map, shareReplay, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';

// Um único refresh em andamento por vez: se várias requisições recebem 401 juntas,
// todas esperam o mesmo token novo em vez de disparar vários /auth/refresh.
let refreshEmAndamento: Observable<string> | null = null;

function comToken(req: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  return token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
}

// Rotas de login e refresh nunca disparam um novo refresh (evita laço).
function ehRotaDeAuth(req: HttpRequest<unknown>): boolean {
  return /\/auth\/(login|refresh)$/.test(req.url);
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(comToken(req, localStorage.getItem('token'))).pipe(
    catchError((erro: unknown) => {
      // 401 = sem login válido (token expirado). 403 é recusa de permissão e não se resolve
      // renovando o token, então segue como erro normal.
      const expirou = erro instanceof HttpErrorResponse && erro.status === 401;
      if (!expirou || ehRotaDeAuth(req) || !localStorage.getItem('refreshToken')) {
        return throwError(() => erro);
      }

      if (!refreshEmAndamento) {
        refreshEmAndamento = auth.refresh().pipe(
          map((resposta) => resposta.token),
          finalize(() => (refreshEmAndamento = null)),
          shareReplay(1),
        );
      }

      return refreshEmAndamento.pipe(
        catchError((erroRefresh: unknown) => {
          auth.encerrarSessao();
          router.navigate(['/login']);
          return throwError(() => erroRefresh);
        }),
        switchMap((tokenNovo) => next(comToken(req, tokenNovo))),
      );
    }),
  );
};

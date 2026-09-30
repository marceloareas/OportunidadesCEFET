import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE_URL } from '../config/app-env';

export type TipoNotificacao =
  | 'CANDIDATURA_RECEBIDA'
  | 'CANDIDATURA_APROVADA'
  | 'OPORTUNIDADE_FINALIZADA'
  | 'COMENTARIO_RECEBIDO';

export interface Notificacao {
  id: string;
  usuarioId: string;
  tipo: TipoNotificacao;
  mensagem: string;
  referenciaId?: string;
  lida: boolean;
  criadoEm: string;
}

@Injectable({ providedIn: 'root' })
export class NotificacaoService {
  private http = inject(HttpClient);
  private readonly API = `${API_BASE_URL}/notificacoes`;

  listar(): Observable<Notificacao[]> {
    return this.http.get<Notificacao[]>(this.API);
  }

  contarNaoLidas(): Observable<number> {
    return this.http
      .get<{ total: number }>(`${this.API}/nao-lidas/contagem`)
      .pipe(map((resposta) => resposta.total));
  }

  marcarComoLida(id: string): Observable<void> {
    return this.http.patch<void>(`${this.API}/${id}/lida`, {});
  }

  marcarTodasComoLidas(): Observable<void> {
    return this.http.patch<void>(`${this.API}/lidas`, {});
  }
}

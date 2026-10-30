import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, of, shareReplay, catchError, tap } from 'rxjs';
import { API_BASE_URL } from '../config/app-env';
import { FeedPage } from './feed.service';

// Quantidade máxima de itens salvos buscada de uma vez (a tela não pagina).
export const LIMITE_ITENS_SALVOS = 200;

@Injectable({
  providedIn: 'root'
})
export class SavedItemsService {
  private api = `${API_BASE_URL}/saved-items`;

  // Cada post do feed pergunta "estou salvo?". Sem cache isso virava uma requisição por
  // post; agora é uma por usuário, invalidada quando ele salva ou remove algo.
  private idsSalvos = new Map<string, Observable<Set<string>>>();

  constructor(private http: HttpClient) {}

  listarPorUsuario(userId: string, page = 0, size = LIMITE_ITENS_SALVOS) {
    return this.http.get<FeedPage>(
      `${this.api}/user/${userId}?page=${page}&size=${size}`
    );
  }

  estaSalvo(userId: string, referenciaId: string): Observable<boolean> {
    if (!this.idsSalvos.has(userId)) {
      this.idsSalvos.set(
        userId,
        this.listarPorUsuario(userId).pipe(
          map((res) => new Set((res.content || []).map((item) => item.referenciaId || item.id || ''))),
          catchError(() => {
            this.idsSalvos.delete(userId);
            return of(new Set<string>());
          }),
          shareReplay(1),
        ),
      );
    }
    return this.idsSalvos.get(userId)!.pipe(map((ids) => ids.has(referenciaId)));
  }

  salvar(userId: string, feedItemId: string) {
    return this.http.post(`${this.api}`, {
      userId,
      feedItemId
    }).pipe(tap(() => this.idsSalvos.delete(userId)));
  }

  remover(userId: string, feedItemId: string) {
    return this.http.delete(
      `${this.api}/${userId}/${feedItemId}`
    ).pipe(tap(() => this.idsSalvos.delete(userId)));
  }
}

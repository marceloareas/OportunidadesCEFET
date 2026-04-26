import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface FeedItem {
  id?: string;
  referenciaId?: string;
  tipoReferencia?: 'POST' | 'OPORTUNIDADE';

  titulo: string;
  corpo?: string;

  criadorId?: string;
  nomeCriador?: string;
  criado?: string | Date;

  imagemBase64?: string;

  ehOportunidade: boolean;

  idLikes?: string[];
  idComentarios?: string[];

  quantidadeDeVagas?: number;
  vagasPreenchidas?: number;
  finalizada?: boolean;
  alunosCandidatosId?: string[];
  alunosAprovadosId?: string[];
}

export interface FeedPage {
  content: FeedItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

@Injectable({
  providedIn: 'root'
})
export class FeedService {
  private api = 'http://localhost:8080/feed';

  constructor(private http: HttpClient) {}

  listar(page = 0, size = 10): Observable<FeedPage> {
    return this.http.get<FeedPage>(`${this.api}?page=${page}&size=${size}`);
  }
}
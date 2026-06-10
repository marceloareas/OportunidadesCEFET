import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface FeedItem {
  id?: string;
  referenciaId?: string;
  tipo?: 'POST' | 'OPORTUNIDADE';

  titulo: string;
  corpo?: string;

  criadorId?: string;
  nomeCriador?: string;
  createdAt?: string | Date;

  imagemBase64?: string;
  imagemPerfil?: string; // base64 ou url da imagem do criador

  idLikes?: string[];
  idComentarios?: string[];
  comentariosCount?: number;

  dataInicioInscricao?: string | Date;
  dataFimInscricao?: string | Date;
  idCategoria?: string;
  grandesAreas?: string[];
  quantidadeDeVagas?: number;
  vagasPreenchidas?: number;
  finalizada?: boolean;
  status?: 'INSCRICOES_EM_BREVE' | 'INSCRICOES_ABERTAS' | 'INSCRICOES_ENCERRADAS' | 'FINALIZADA';
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
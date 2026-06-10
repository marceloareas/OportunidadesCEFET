import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
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

export interface FeedFiltros {
  status?: string;
  categoria?: string;
  area?: string;
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

  listar(page = 0, size = 10, filtros?: FeedFiltros): Observable<FeedPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (filtros?.status) params = params.set('status', filtros.status);
    if (filtros?.categoria) params = params.set('categoria', filtros.categoria);
    if (filtros?.area) params = params.set('area', filtros.area);

    return this.http.get<FeedPage>(this.api, { params });
  }
}
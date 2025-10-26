import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Oportunidade {
  id?: string;
  nome: string;
  descricao?: string;
  professorId: string;
  quantidadeDeVagas?: number;
  vagasPreenchidas?: number;
  idCategoria?: string;
  idLikes?: string[];
  criado?: Date | string;
  imagemBase64?: string; // ✅ suporte à imagem
}

@Injectable({
  providedIn: 'root'
})
export class OportunidadeService {
  private apiUrl = 'http://localhost:8080/oportunities';

  constructor(private http: HttpClient) {}

  listar(): Observable<Oportunidade[]> {
    return this.http.get<Oportunidade[]>(this.apiUrl);
  }

  buscarPorId(id: string): Observable<Oportunidade> {
    return this.http.get<Oportunidade>(`${this.apiUrl}/${id}`);
  }

  criar(oportunidade: Oportunidade): Observable<Oportunidade> {
    return this.http.post<Oportunidade>(this.apiUrl, oportunidade);
  }

  deletar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

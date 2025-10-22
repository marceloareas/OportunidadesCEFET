import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// 🔹 Interface de dados (mantém compatibilidade com backend)
export interface Oportunidade {
  id?: string;
  nome: string;
  descricao?: string;
  professorId: string;
  quantidadeDeVagas: number;
  vagasPreenchidas?: number;
  idCategoria?: string;
  criado?: string | Date;
  idMembros?: string[];
  idImagens?: string[];
  idLikes?: string[];
  idCandidatos?: string[];
}

// 🔹 Serviço principal de comunicação com o backend
@Injectable({ providedIn: 'root' })
export class OportunidadeService {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/oportunities'; // endpoint do backend

  listar(): Observable<Oportunidade[]> {
    return this.http.get<Oportunidade[]>(this.API);
  }

  criar(oportunidade: Oportunidade): Observable<Oportunidade> {
    return this.http.post<Oportunidade>(this.API, oportunidade);
  }

  atualizar(id: string, oportunidade: Oportunidade): Observable<Oportunidade> {
    return this.http.put<Oportunidade>(`${this.API}/${id}`, oportunidade);
  }

  deletar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}

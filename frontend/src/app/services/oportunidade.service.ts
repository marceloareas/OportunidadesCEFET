import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Oportunidade {
  id?: string;
  nome: string;
  descricao?: string;
  professorId?: string;
  quantidadeDeVagas?: number;
  vagasPreenchidas?: number;
  idCategoria?: string;
  grandesAreas?: string[];
  imagemBase64?: string;
  criado?: string | Date;
  alunosCandidatosId?: string[];
  idLikes?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class OportunidadeService {
  private apiUrl = 'http://localhost:8080/oportunidades';

  constructor(private http: HttpClient) {}

  listar(): Observable<Oportunidade[]> {
    return this.http.get<Oportunidade[]>(this.apiUrl);
  }

  criar(oportunidade: Oportunidade): Observable<Oportunidade> {
    return this.http.post<Oportunidade>(this.apiUrl, oportunidade);
  }

  atualizar(id: string, oportunidade: Oportunidade): Observable<Oportunidade> {
    return this.http.put<Oportunidade>(`${this.apiUrl}/${id}`, oportunidade);
  }

  deletar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  candidatarAluno(idOportunidade: string, idAluno: string) {
    return this.http.post<Oportunidade>(
      `${this.apiUrl}/${idOportunidade}/candidatar/${idAluno}`,
      {}
    );
  }

  curtirOportunidade(idOportunidade: string, idUsuario: string) {
    return this.http.post(`${this.apiUrl}/${idOportunidade}/like/${idUsuario}`, {});
  }

  aprovarCandidato(idOportunidade: string, idAluno: string) {
    return this.http.post<Oportunidade>(`${this.apiUrl}/${idOportunidade}/aprovar/${idAluno}`, {});
  }

}

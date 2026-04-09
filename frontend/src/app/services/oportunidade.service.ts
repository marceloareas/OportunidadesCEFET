import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/app-env';
import { Usuario } from './usuario.service';

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
  alunosAprovadosId?: string[];
  finalizada?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class OportunidadeService {
  private apiUrl = `${API_BASE_URL}/oportunidades`;

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

  aprovarCandidatoDoProfessor(idOportunidade: string, idAluno: string, idProfessor: string) {
    return this.http.post<Oportunidade>(
      `${this.apiUrl}/${idOportunidade}/aprovar/${idAluno}/professor/${idProfessor}`,
      {}
    );
  }

  finalizarOportunidade(idOportunidade: string) {
    return this.http.post<Oportunidade>(`${this.apiUrl}/${idOportunidade}/finalizar`, {});
  }

  listarCandidatosDoProfessor(idOportunidade: string, idProfessor: string) {
    return this.http.get<Usuario[]>(`${this.apiUrl}/${idOportunidade}/candidatos/professor/${idProfessor}`);
  }

}

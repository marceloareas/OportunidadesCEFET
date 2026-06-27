import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/app-env';

export interface Comentario {
  id?: string;
  usuarioId?: string;
  idComentarioPai?: string | null;
  tipoEntidadePai?: string;
  idPost?: string;
  createdAt?: string | Date;
  texto?: string;
  idLikes?: string[];
}

@Injectable({ providedIn: 'root' })
export class ComentarioService {
  private http = inject(HttpClient);
  private readonly API = `${API_BASE_URL}/comments`;

  listarComentariosPost(idPost: string): Observable<Comentario[]> {
    return this.http.get<Comentario[]>(`${this.API}/post/${idPost}`);
  }

  listarComentariosOportunidade(idOportunidade: string): Observable<Comentario[]> {
    return this.http.get<Comentario[]>(`${this.API}/oportunidade/${idOportunidade}`);
  }

  criar(comentario: Comentario): Observable<Comentario> {
    return this.http.post<Comentario>(this.API, comentario);
  }
}

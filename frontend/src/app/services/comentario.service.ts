import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Comentario {
  id?: string;
  usuarioId?: string;
  idComentarioPai?: string | null;
  tipoEntidadePai?: string;
  idPost?: string;
  dataComentario?: string | Date;
  texto?: string;
  idLikes?: string[];
}

@Injectable({ providedIn: 'root' })
export class ComentarioService {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/comments';

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

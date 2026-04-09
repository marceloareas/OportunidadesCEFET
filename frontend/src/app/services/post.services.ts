import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/app-env';

export interface Post {
  id?: string;
  titulo: string;
  corpo: string;
  criadorId?: string;
  nomeCriador?: string;
  criado?: string | Date;
  idLikes?: string[];
  idComentarios?: any[];
  imagemBase64?: string;
  ehOportunidade?: boolean;
  finalizada?: boolean;
  vagasPreenchidas?: number;
  quantidadeDeVagas?: number;
  alunosCandidatosId?: string[];
  alunosAprovadosId?: string[];
}


@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = `${API_BASE_URL}/posts`;

  constructor(private http: HttpClient) {}

  getPosts(): Observable<Post[]> {
    return this.http.get<Post[]>(this.apiUrl);
  }

  createPost(post: Post): Observable<Post> {
    return this.http.post<Post>(this.apiUrl, post);
  }

  atualizarLike(postId: string, usuarioId: string) {
    return this.http.post(`${this.apiUrl}/${postId}/like/${usuarioId}`, {});
  }

}

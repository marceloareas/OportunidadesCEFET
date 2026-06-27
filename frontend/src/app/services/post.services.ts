import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/app-env';

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

export interface Post {
  id?: string;
  titulo: string;
  corpo: string;
  criadorId?: string;
  nomeCriador?: string;
  createdAt?: string | Date;
  idLikes?: string[];
  idComentarios?: any[];
  imagemBase64?: string;
  finalizada?: boolean;
  vagasPreenchidas?: number;
  quantidadeDeVagas?: number;
  alunosCandidatosId?: string[];
  alunosAprovadosId?: string[];
  imagemPerfil?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = `${API_BASE_URL}/posts`;

  constructor(private http: HttpClient) {}

  getPosts(page: number = 0, size: number = 10): Observable<Page<Post>> {
    return this.http.get<Page<Post>>(
      `${this.apiUrl}?page=${page}&size=${size}`
    );
  }

  getPostsByUser(userId: string, page: number = 0, size: number = 10) {
    return this.http.get<Page<Post>>(
      `${this.apiUrl}/mine/${userId}?page=${page}&size=${size}`
    );
  }

  createPost(post: Post): Observable<Post> {
    return this.http.post<Post>(this.apiUrl, post);
  }

  atualizarLike(postId: string, usuarioId: string) {
    return this.http.post(`${this.apiUrl}/${postId}/like/${usuarioId}`, {});
  }
}
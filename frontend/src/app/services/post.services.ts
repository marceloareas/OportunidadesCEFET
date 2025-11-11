import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Post {
  id?: string;
  titulo: string;
  corpo: string;
  criadorId?: string;
  nomeCriador?: string;
  criado?: string | Date;
  likesId?: string[];
  idComentarios?: any[];
  imagemBase64?: string;
  ehOportunidade?: boolean;

  // 🔹 Campos extras usados apenas quando é uma oportunidade
  vagasPreenchidas?: number;
  quantidadeDeVagas?: number;
  alunosCandidatosId?: string[];
}


@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = 'http://localhost:8080/posts'; // URL do seu backend Spring Boot

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

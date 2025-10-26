import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Usuario {
  id?: string;
  nome: string;
  email: string;
  senha: string;
  funcao?: string;     // 'Aluno', 'Professor'
  matricula?: string;  // campo adicional do frontend
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/users';

  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.API);
  }

  cadastrar(usuario: Usuario): Observable<Usuario> {
    return this.http.post<Usuario>(this.API, usuario);
  }

  buscarPorId(id: string): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.API}/${id}`);
  }

  atualizar(id: string, usuario: Usuario): Observable<Usuario> {
    return this.http.put<Usuario>(`${this.API}/${id}`, usuario);
  }

  deletar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API}/${id}`);
  }
}

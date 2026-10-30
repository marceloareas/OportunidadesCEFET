import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config/app-env';
import { Usuario } from './usuario.service';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  usuario: Usuario;
}

// Chaves da sessão no localStorage. Outras preferências (ex.: tema) ficam fora daqui
// e sobrevivem ao logout.
export const CHAVES_SESSAO = ['isLogged', 'token', 'refreshToken', 'usuario', 'tipoUsuario'];

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private readonly API = `${API_BASE_URL}/auth`;

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/login`, payload);
  }

  // Troca o refresh token guardado por um novo par e já grava os tokens novos (B5).
  refresh(): Observable<AuthResponse> {
    const refreshToken = localStorage.getItem('refreshToken');
    return this.http
      .post<AuthResponse>(`${this.API}/refresh`, { refreshToken })
      .pipe(tap((resposta) => this.guardarTokens(resposta)));
  }

  guardarTokens(resposta: Pick<AuthResponse, 'token' | 'refreshToken'>): void {
    localStorage.setItem('token', resposta.token);
    if (resposta.refreshToken) {
      localStorage.setItem('refreshToken', resposta.refreshToken);
    }
  }

  encerrarSessao(): void {
    CHAVES_SESSAO.forEach((chave) => localStorage.removeItem(chave));
  }
}

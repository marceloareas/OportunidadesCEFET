import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/app-env';
import { Usuario } from './usuario.service';
import { Page } from './post.services';

// Sprint 6: moderação de usuários (só o administrador; a API devolve 403 para os outros).
@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private readonly API = `${API_BASE_URL}/admin/usuarios`;

  listarUsuarios(busca: string, page = 0, size = 10): Observable<Page<Usuario>> {
    let params = new HttpParams().set('page', page).set('size', size);
    const termo = busca.trim();
    if (termo) params = params.set('busca', termo);
    return this.http.get<Page<Usuario>>(this.API, { params });
  }

  desativar(id: string): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.API}/${id}/desativar`, {});
  }

  reativar(id: string): Observable<Usuario> {
    return this.http.patch<Usuario>(`${this.API}/${id}/reativar`, {});
  }
}

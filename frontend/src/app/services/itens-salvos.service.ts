import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE_URL } from '../config/app-env';
import { FeedPage } from './feed.service';

@Injectable({
  providedIn: 'root'
})
export class SavedItemsService {
  private api = `${API_BASE_URL}/saved-items`;

  constructor(private http: HttpClient) {}

  listarPorUsuario(userId: string, page = 0, size = 10) {
    return this.http.get<FeedPage>(
      `${this.api}/user/${userId}?page=${page}&size=${size}`
    );
  }

  salvar(userId: string, feedItemId: string) {
    return this.http.post(`${this.api}`, {
      userId,
      feedItemId
    });
  }

  remover(userId: string, feedItemId: string) {
    return this.http.delete(
      `${this.api}/${userId}/${feedItemId}`
    );
  }
}
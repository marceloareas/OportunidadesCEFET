import { EnvironmentProviders, Provider } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';

// Providers que quase todo componente precisa em teste: HTTP falso, rotas vazias e
// animações desligadas (MatDialog/MatSnackBar). Evita o NG0201 de HttpClient (M3).
export function providersDeTeste(): Array<Provider | EnvironmentProviders> {
  return [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), provideNoopAnimations()];
}

export interface UsuarioDeTeste {
  id: string;
  nome: string;
  funcao: 'ALUNO' | 'PROFESSOR';
  email?: string;
}

// Simula o usuário logado do jeito que o login grava no localStorage.
export function logarComo(usuario: UsuarioDeTeste): void {
  localStorage.setItem('isLogged', 'true');
  localStorage.setItem('token', 'token-de-teste');
  localStorage.setItem('usuario', JSON.stringify(usuario));
  localStorage.setItem('tipoUsuario', usuario.funcao === 'PROFESSOR' ? 'professor' : 'aluno');
}

export function deslogar(): void {
  ['isLogged', 'token', 'refreshToken', 'usuario', 'tipoUsuario'].forEach((chave) =>
    localStorage.removeItem(chave),
  );
}

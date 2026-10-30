import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login.component';
import { EditarPerfil } from './pages/editar-perfil/editar-perfil'
import { MinhasDiscussoes } from './pages/minhas-discussoes/minhas-discussoes';
import { OportunidadesPage } from './pages/oportunidades/oportunidades';
import { ItensSalvosComponent } from './pages/itens-salvos/itens-salvos';
import { Perfil } from './pages/perfil/perfil';
import { authGuard, visitanteGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login, canActivate: [visitanteGuard] },
  { path: 'home', component: Home, canActivate: [authGuard] },
  { path: 'config', component: EditarPerfil, canActivate: [authGuard] },
  { path: 'minhas_discussoes', component: MinhasDiscussoes, canActivate: [authGuard] },
  { path: 'oportunidades', component: OportunidadesPage, canActivate: [authGuard] },
  { path: 'itens_salvos', component: ItensSalvosComponent, canActivate: [authGuard] },
  { path: 'perfil/:id', component: Perfil, canActivate: [authGuard] },
  // Endereço inexistente: manda para o feed (ou para o login, pela guarda).
  { path: '**', redirectTo: 'home' }
];

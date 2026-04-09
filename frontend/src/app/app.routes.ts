import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login.component';
import { EditarPerfil } from './pages/editar-perfil/editar-perfil'
import { MinhasDiscussoes } from './pages/minhas-discussoes/minhas-discussoes';
import { OportunidadesPage } from './pages/oportunidades/oportunidades';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'home', component: Home },
  { path: 'config', component: EditarPerfil },
  { path: 'minhas_discussoes', component: MinhasDiscussoes },
  { path: 'oportunidades', component: OportunidadesPage }
];

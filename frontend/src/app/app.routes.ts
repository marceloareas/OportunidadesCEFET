import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login.component';
import { EditarPerfil } from './pages/editar-perfil/editar-perfil'

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'home', component: Home },
  { path: 'editar_perfil', component: EditarPerfil }
];

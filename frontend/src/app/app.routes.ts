import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'home', component: Home },
];

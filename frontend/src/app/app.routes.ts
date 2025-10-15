// src/app/app.routes.ts

import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login.component'

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'login', component: Login }
];

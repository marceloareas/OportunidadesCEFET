import { Component } from '@angular/core';
import { Post } from '../../components/post/post';
import { CommonModule } from '@angular/common';
import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [Post, CommonModule, NavbarTop, NavbarLeft, NavbarRight],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {
  currentView: string = 'home';

  constructor(private router: Router) { }

  isLogged: boolean = false; // Simulação de estado de login

  ngOnInit(): void {
    if (!this.isLogged) {
      // redireciona para /login se não estiver logado
      this.router.navigate(['/login']);
    }
  }

  setView(view: string) {
    this.currentView = view;
  }

  // abrir modal new post
  newPostModal = false;

  openNewPost() {
    this.newPostModal = true;
  }

  closeNewPost() {
    this.newPostModal = false;
  }
}

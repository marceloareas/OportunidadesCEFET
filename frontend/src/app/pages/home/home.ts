import { Component } from '@angular/core';
import { Post } from '../../components/post/post';
import { CommonModule } from '@angular/common';
import { NavbarTop } from '../../components/navbar-top/navbar-top';
import { NavbarLeft } from '../../components/navbar-left/navbar-left';
import { NavbarRight } from '../../components/navbar-right/navbar-right';

@Component({
  selector: 'app-home',
  imports: [Post, CommonModule, NavbarTop, NavbarLeft, NavbarRight],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {
  currentView: string = 'home';

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

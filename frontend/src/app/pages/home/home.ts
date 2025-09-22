import { Component } from '@angular/core';
import { Post } from '../../components/post/post';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  imports: [Post, CommonModule],
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

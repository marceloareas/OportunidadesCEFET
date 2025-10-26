import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Post as PostModel } from '../../services/post.services'; // ✅ usa o modelo do service

@Component({
  selector: 'app-post',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './post.html',
  styleUrl: './post.css'
})
export class PostComponent {
  @Input() post!: PostModel; // ✅ evita conflito de tipo
}

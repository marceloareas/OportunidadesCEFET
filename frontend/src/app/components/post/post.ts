import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-post',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './post.html',
  styleUrl: './post.css'
})
export class PostComponent {
  @Input() post!: {
    id?: string;
    titulo: string;
    corpo: string;
    criadorId?: string;
    criado?: string | Date;
    likesId?: string[];
    idComentarios?: any[];
    imagemBase64?: string;
  };
}

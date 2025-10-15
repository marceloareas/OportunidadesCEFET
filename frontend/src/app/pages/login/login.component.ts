import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class Login {
  isRegistering = false;
  opcoes = ['Aluno', 'Professor'];
  selecionada = 'Aluno';

  toggleForm() {
    this.isRegistering = !this.isRegistering;
  }
}

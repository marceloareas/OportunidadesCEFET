import { Component, inject } from '@angular/core';
import { NotificacoesSino } from '../notificacoes-sino/notificacoes-sino';
import { ThemeService } from '../../services/theme.service';

@Component({
  selector: 'app-navbar-top',
  imports: [NotificacoesSino],
  templateUrl: './navbar-top.html',
  styleUrl: './navbar-top.css'
})
export class NavbarTop {
  readonly tema = inject(ThemeService);
}

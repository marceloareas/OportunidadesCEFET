import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'app-navbar-right',
  imports: [CommonModule],
  templateUrl: './navbar-right.html',
  styleUrl: './navbar-right.css'
})
export class NavbarRight {
  @Input() logged!: boolean;
}

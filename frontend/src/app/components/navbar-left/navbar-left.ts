import { Component } from '@angular/core';
import { Router } from '@angular/router';


@Component({
  selector: 'app-navbar-left',
  imports: [],
  templateUrl: './navbar-left.html',
  styleUrl: './navbar-left.css'
})
export class NavbarLeft {

  constructor(private router: Router) {}

  setView(view: string) {
    this.router.navigate([`/${view}`]);
  }  
}

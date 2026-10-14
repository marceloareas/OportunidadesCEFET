import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { NavbarTop } from './navbar-top';

describe('NavbarTop', () => {
  let component: NavbarTop;
  let fixture: ComponentFixture<NavbarTop>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarTop],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavbarTop);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

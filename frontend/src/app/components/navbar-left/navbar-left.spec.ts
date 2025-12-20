import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarLeft } from './navbar-left';

describe('NavbarLeft', () => {
  let component: NavbarLeft;
  let fixture: ComponentFixture<NavbarLeft>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarLeft]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavbarLeft);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

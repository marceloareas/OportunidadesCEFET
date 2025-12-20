import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarRight } from './navbar-right';

describe('NavbarRight', () => {
  let component: NavbarRight;
  let fixture: ComponentFixture<NavbarRight>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarRight]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavbarRight);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

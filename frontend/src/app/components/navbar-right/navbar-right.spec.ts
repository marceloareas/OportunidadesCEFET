import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarRight } from './navbar-right';
import { providersDeTeste } from '../../../testing/test-providers';

describe('NavbarRight', () => {
  let component: NavbarRight;
  let fixture: ComponentFixture<NavbarRight>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarRight],
      providers: providersDeTeste()
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

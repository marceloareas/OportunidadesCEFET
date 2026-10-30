import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarLeft } from './navbar-left';
import { providersDeTeste } from '../../../testing/test-providers';

describe('NavbarLeft', () => {
  let component: NavbarLeft;
  let fixture: ComponentFixture<NavbarLeft>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarLeft],
      providers: providersDeTeste()
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

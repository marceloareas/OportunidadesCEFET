import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditarPerfil } from './editar-perfil';
import { providersDeTeste } from '../../../testing/test-providers';

describe('EditarPerfil', () => {
  let component: EditarPerfil;
  let fixture: ComponentFixture<EditarPerfil>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditarPerfil],
      providers: providersDeTeste()
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditarPerfil);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

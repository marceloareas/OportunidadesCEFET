import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ItensSalvosComponent } from './itens-salvos';

describe('ItensSalvosComponent', () => {
  let component: ItensSalvosComponent;
  let fixture: ComponentFixture<ItensSalvosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItensSalvosComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ItensSalvosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
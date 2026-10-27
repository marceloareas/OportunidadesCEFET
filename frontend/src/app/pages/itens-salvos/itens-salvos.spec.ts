import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ItensSalvosComponent } from './itens-salvos';
import { providersDeTeste } from '../../../testing/test-providers';

describe('ItensSalvosComponent', () => {
  let component: ItensSalvosComponent;
  let fixture: ComponentFixture<ItensSalvosComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItensSalvosComponent],
      providers: providersDeTeste()
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

import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FeedbackService } from './feedback.service';

describe('FeedbackService', () => {
  let service: FeedbackService;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  beforeEach(() => {
    snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);
    TestBed.configureTestingModule({
      providers: [FeedbackService, { provide: MatSnackBar, useValue: snackBarSpy }],
    });
    service = TestBed.inject(FeedbackService);
  });

  it('success() abre o snackbar com o painel de sucesso', () => {
    service.success('Perfil atualizado com sucesso.');
    expect(snackBarSpy.open).toHaveBeenCalledWith(
      'Perfil atualizado com sucesso.',
      'Fechar',
      jasmine.objectContaining({ panelClass: ['notification-success'] })
    );
  });

  it('error() abre o snackbar com o painel de erro', () => {
    service.error('Erro ao se candidatar à vaga.');
    expect(snackBarSpy.open).toHaveBeenCalledWith(
      'Erro ao se candidatar à vaga.',
      'Fechar',
      jasmine.objectContaining({ panelClass: ['notification-error'] })
    );
  });
});

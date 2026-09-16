import { TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { ConfirmDialogService } from './confirm-dialog.service';

describe('ConfirmDialogService', () => {
  let service: ConfirmDialogService;
  let dialogSpy: jasmine.SpyObj<MatDialog>;

  beforeEach(() => {
    const dialogRefSpy = { afterClosed: () => of(true) } as MatDialogRef<any, boolean>;
    dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    dialogSpy.open.and.returnValue(dialogRefSpy);

    TestBed.configureTestingModule({
      providers: [ConfirmDialogService, { provide: MatDialog, useValue: dialogSpy }],
    });
    service = TestBed.inject(ConfirmDialogService);
  });

  it('confirm() abre o ConfirmDialogComponent com os dados informados e repassa o resultado', (done) => {
    service.confirm({ title: 'Finalizar', message: 'Tem certeza?' }).subscribe((result) => {
      expect(result).toBe(true);
      done();
    });

    expect(dialogSpy.open).toHaveBeenCalled();
    const dataArg = dialogSpy.open.calls.mostRecent().args[1]?.data;
    expect(dataArg).toEqual({ title: 'Finalizar', message: 'Tem certeza?' });
  });
});

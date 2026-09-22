import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';
import { ConfirmDialogComponent, ConfirmDialogData } from '../components/confirm-dialog/confirm-dialog';

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private dialog = inject(MatDialog);

  confirm(data: ConfirmDialogData): Observable<boolean> {
    const ref = this.dialog.open(ConfirmDialogComponent, { data, width: '400px' });
    return ref.afterClosed().pipe(map((result) => result === true));
  }
}

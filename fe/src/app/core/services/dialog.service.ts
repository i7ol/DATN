// app/core/services/dialog.service.ts
import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { AuthModalComponent } from '../../shared/components/auth-modal/auth-modal.component';

@Injectable({
  providedIn: 'root',
})
export class DialogService {
  private authDialogRef?: MatDialogRef<AuthModalComponent>;

  constructor(private dialog: MatDialog) {}

  openAuthModal(mode: 'login' | 'register' = 'login'): void {
    if (this.authDialogRef) {
      this.authDialogRef.close();
    }

    this.authDialogRef = this.dialog.open(AuthModalComponent, {
      width: '400px',
      maxWidth: '95vw',
      panelClass: 'auth-modal-dialog',
      backdropClass: 'auth-modal-backdrop',
      disableClose: false,
      autoFocus: false,
      data: { mode },
    });

    this.authDialogRef.afterClosed().subscribe(() => {
      this.authDialogRef = undefined;
    });
  }

  closeAuthModal(): void {
    if (this.authDialogRef) {
      this.authDialogRef.close();
    }
  }
}

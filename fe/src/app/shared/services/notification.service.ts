import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  constructor(private snackBar: MatSnackBar) {}

  success(message: string, duration = 3000) {
    this.open(message, duration, 'snackbar-success');
  }

  error(message: string, duration = 3000) {
    this.open(message, duration, 'snackbar-error');
  }

  info(message: string, duration = 3000) {
    this.open(message, duration, 'snackbar-info');
  }

  private open(message: string, duration: number, panelClass: string) {
    this.snackBar.open(message, '✕', {
      duration,
      horizontalPosition: 'left',
      verticalPosition: 'bottom',
      panelClass,
    });
  }
}

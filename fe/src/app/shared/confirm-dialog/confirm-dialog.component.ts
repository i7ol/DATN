import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

export interface ConfirmDialogData {
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
}

@Component({
  selector: 'confirm-dialog',
  template: `
    <h2 mat-dialog-title>{{ data.title || 'Xác nhận' }}</h2>

    <mat-dialog-content>
      <p>{{ data.message }}</p>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="close(false)">
        {{ data.cancelText || 'Hủy' }}
      </button>

      <button mat-raised-button color="warn" (click)="close(true)">
        {{ data.confirmText || 'Đồng ý' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ConfirmDialogComponent {
  constructor(
    private dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData
  ) {}

  close(result: boolean) {
    this.dialogRef.close(result);
  }
}

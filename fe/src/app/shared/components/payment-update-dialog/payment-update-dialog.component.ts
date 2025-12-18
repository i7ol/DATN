import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

export interface PaymentUpdateDialogData {
  paymentId?: number;
  currentStatus?: string;
  targetStatus?: string;
  currentAmount?: number;
  currentTransactionId?: string;
  currentNote?: string;
}

@Component({
  selector: 'app-payment-update-dialog',
  templateUrl: './payment-update-dialog.component.html',
  styleUrls: ['./payment-update-dialog.component.scss'],
})
export class PaymentUpdateDialogComponent implements OnInit {
  paymentForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<PaymentUpdateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PaymentUpdateDialogData
  ) {
    this.paymentForm = this.fb.group({
      amount: [
        data.currentAmount || 0,
        [Validators.required, Validators.min(0)],
      ],
      transactionId: [
        data.currentTransactionId || '',
        this.shouldShowTransactionId() ? Validators.required : [],
      ],
      note: [data.currentNote || ''],
    });
  }

  ngOnInit(): void {
    // Cập nhật validators dựa trên targetStatus
    if (this.data.targetStatus === 'PAID') {
      this.paymentForm
        .get('transactionId')
        ?.setValidators([Validators.required]);
    }
    this.paymentForm.updateValueAndValidity();
  }

  getDialogTitle(): string {
    if (this.data.targetStatus === 'PAID') {
      return 'Đánh dấu đã thanh toán';
    } else if (this.data.targetStatus === 'REFUNDED') {
      return 'Hoàn tiền';
    }
    return 'Cập nhật thanh toán';
  }

  getSubmitButtonText(): string {
    if (this.data.targetStatus === 'PAID') {
      return 'Đánh dấu đã TT';
    } else if (this.data.targetStatus === 'REFUNDED') {
      return 'Hoàn tiền';
    }
    return 'Cập nhật';
  }

  shouldShowAmount(): boolean {
    return true; // Luôn hiển thị amount
  }

  shouldShowTransactionId(): boolean {
    return this.data.targetStatus === 'PAID';
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.paymentForm.valid) {
      this.dialogRef.close(this.paymentForm.value);
    }
  }
}

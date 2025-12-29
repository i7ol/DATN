import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

export interface StatusUpdateDialogData {
  id: number;
  currentStatus: string;
  type: 'order' | 'shipping' | 'payment';
  statusOptions?: Array<{ value: string; label: string }>;
}

@Component({
  selector: 'app-status-update-dialog',
  templateUrl: './status-update-dialog.component.html',
  styleUrls: ['./status-update-dialog.component.scss'],
})
export class StatusUpdateDialogComponent {
  statusForm: FormGroup;
  statusOptions: Array<{ value: string; label: string }> = [];

  constructor(
    public dialogRef: MatDialogRef<StatusUpdateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: StatusUpdateDialogData,
    private fb: FormBuilder
  ) {
    // SỬA: Sử dụng statusOptions từ data nếu có, nếu không dùng mặc định
    this.statusOptions = data.statusOptions || [
      { value: 'PREPARING', label: 'Đang chuẩn bị' },
      { value: 'READY_TO_SHIP', label: 'Sẵn sàng giao' },
      { value: 'SHIPPED', label: 'Đã gửi hàng' },
      { value: 'IN_TRANSIT', label: 'Đang vận chuyển' },
      { value: 'OUT_FOR_DELIVERY', label: 'Đang giao hàng' },
      { value: 'DELIVERED', label: 'Đã giao hàng' },
      { value: 'CANCELLED', label: 'Đã hủy' },
      { value: 'RETURNED', label: 'Đã trả hàng' },
      { value: 'FAILED', label: 'Giao hàng thất bại' },
    ];

    this.statusForm = this.fb.group({
      status: [data.currentStatus || '', Validators.required],
      notes: [''],
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.statusForm.valid) {
      this.dialogRef.close(this.statusForm.value);
    }
  }
}

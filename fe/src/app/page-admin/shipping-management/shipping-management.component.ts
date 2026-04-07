import { ShippingAdminControllerService } from 'src/app/api/admin';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';

import { ShippingResponse } from 'src/app/api/admin';
import { StatusUpdateDialogComponent } from 'src/app/shared/components/status-update-dialog/status-update-dialog.component';

@Component({
  selector: 'app-shipping-management',
  templateUrl: './shipping-management.component.html',
  styleUrls: ['./shipping-management.component.scss'],
})
export class ShippingManagementComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  shippingForm!: FormGroup;
  dataSource = new MatTableDataSource<ShippingResponse>();
  loading = false;
  orderId!: number;

  displayedColumns: string[] = [
    'id',
    'trackingCode',
    'shippingCompany',
    'recipientName',
    'status',
    'createdAt',
    'actions',
  ];

  statusOptions = [
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

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private shippingService: ShippingAdminControllerService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.orderId = Number(this.route.snapshot.paramMap.get('id'));
    this.initForm();
    if (this.orderId) {
      this.loadShippingInfo();
    }
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  private initForm(): void {
    this.shippingForm = this.fb.group({
      shippingCompany: ['Viettel', Validators.required],
      shippingMethod: ['STANDARD'],
      trackingCode: [{ value: '', disabled: true }],
      shippingFee: [30000, [Validators.required, Validators.min(0)]],
      estimatedDeliveryDays: [3],
      notes: [''],
    });
  }

  loadShippingInfo(): void {
    this.loading = true;
    this.shippingService.getByOrderId(this.orderId).subscribe({
      next: async (res) => {
        const data = await parseBlobJson<ShippingResponse[]>(res);
        this.dataSource.data = data;
        this.loading = false;
      },
      error: () => {
        this.showError('Lỗi khi tải thông tin vận chuyển');
        this.loading = false;
      },
    });
  }

  createShipping(): void {
    console.log('CLICKED');
    console.log('FORM VALID:', this.shippingForm.valid);
    console.log('ORDER ID:', this.orderId);
    if (!this.shippingForm.valid || !this.orderId) return;

    this.loading = true;

    const body = { ...this.shippingForm.value, orderId: this.orderId };

    this.shippingService.create(body).subscribe({
      next: async (res) => {
        const shipping = await parseBlobJson<ShippingResponse>(res);
        this.dataSource.data = [shipping, ...this.dataSource.data];

        this.shippingForm.reset({
          shippingMethod: 'STANDARD',
          shippingFee: 0,
          estimatedDeliveryDays: 3,
        });

        this.loading = false;
        this.showSuccess('Tạo vận chuyển thành công');
      },
      error: (err) => {
        this.loading = false;
        this.showError(err.error?.message || 'Tạo vận chuyển thất bại');
      },
    });
  }

  updateStatus(id: number, currentStatus: string): void {
    const dialogRef = this.dialog.open(StatusUpdateDialogComponent, {
      width: '400px',
      data: {
        id,
        currentStatus,
        type: 'shipping',
        statusOptions: this.statusOptions,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (!result) return;

      this.shippingService
        .updateStatus(id, {
          status: result.status,
          notes: result.notes || '',
        })
        .subscribe({
          next: async (res) => {
            const updated = await parseBlobJson<ShippingResponse>(res);
            const idx = this.dataSource.data.findIndex((s) => s.id === id);
            if (idx !== -1) {
              const clone = [...this.dataSource.data];
              clone[idx] = updated;
              this.dataSource.data = clone;
            }
            this.showSuccess('Cập nhật trạng thái thành công');
          },
          error: () => this.showError('Cập nhật trạng thái thất bại'),
        });
    });
  }

  formatDate(date: string | undefined): string {
    if (!date) return '';
    return new Date(date).toLocaleString('vi-VN');
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'DELIVERED':
        return 'bg-green-100 text-green-700';
      case 'CANCELLED':
      case 'FAILED':
        return 'bg-red-100 text-red-700';
      case 'SHIPPED':
      case 'IN_TRANSIT':
        return 'bg-blue-100 text-blue-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }
  syncWithProvider(id: number): void {
    this.shippingService.syncWithProvider(id).subscribe({
      next: async (res) => {
        const updated = await parseBlobJson<ShippingResponse>(res);
        const idx = this.dataSource.data.findIndex((s) => s.id === id);
        if (idx !== -1) {
          const clone = [...this.dataSource.data];
          clone[idx] = updated;
          this.dataSource.data = clone;
        }
        this.showSuccess('Đồng bộ thành công');
      },
      error: () => this.showError('Đồng bộ thất bại'),
    });
  }

  getStatusLabel(status: string): string {
    return this.statusOptions.find((s) => s.value === status)?.label || status;
  }

  private showSuccess(msg: string): void {
    this.snackBar.open(msg, 'Đóng', {
      duration: 3000,
      panelClass: ['success-snackbar'],
    });
  }

  private showError(msg: string): void {
    this.snackBar.open(msg, 'Đóng', {
      duration: 5000,
      panelClass: ['error-snackbar'],
    });
  }
}

/* ====== parseBlobJson ====== */
export function parseBlobJson<T>(body: any): Promise<T> {
  if (body instanceof Blob) {
    return body.text().then((text) => JSON.parse(text) as T);
  }
  return Promise.resolve(body as T);
}

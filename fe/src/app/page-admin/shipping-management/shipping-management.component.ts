import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ShippingResponse } from 'src/app/api/admin';

import { ShippingAdminControllerService } from 'src/app/api/admin/api/shippingAdminController.service';

import { StatusUpdateDialogComponent } from 'src/app/shared/components/status-update-dialog/status-update-dialog.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-shipping-management',
  templateUrl: './shipping-management.component.html',
  styleUrls: ['./shipping-management.component.scss'],
})
export class ShippingManagementComponent implements OnInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  shippingForm: FormGroup;
  dataSource = new MatTableDataSource<ShippingResponse>();
  displayedColumns: string[] = [
    'id',
    'trackingNumber',
    'shippingCompany',
    'recipientName',
    'status',
    'createdAt',
    'actions',
  ];

  // SỬA: Cập nhật statusOptions theo StatusEnum mới
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

  loading = false;
  orderId!: number;

  constructor(
    private route: ActivatedRoute,
    private fb: FormBuilder,
    private shippingService: ShippingAdminControllerService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.shippingForm = this.fb.group({
      shippingCompany: ['', Validators.required],
      shippingMethod: ['STANDARD'],
      trackingNumber: [''],
      shippingFee: [0, [Validators.required, Validators.min(0)]],
      estimatedDeliveryDays: [3],
      notes: [''],
    });
  }

  ngOnInit(): void {
    this.orderId = +this.route.snapshot.paramMap.get('orderId')!;
    if (this.orderId) {
      this.loadShippingInfo();
    }
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadShippingInfo(): void {
    this.loading = true;
    this.shippingService.getByOrderId(this.orderId).subscribe({
      next: (shippingList) => {
        this.dataSource.data = shippingList;
        this.loading = false;
      },
      error: (error) => {
        this.showError('Lỗi khi tải thông tin vận chuyển');
        this.loading = false;
        console.error('Error loading shipping info:', error);
      },
    });
  }

  createShipping(): void {
    if (this.shippingForm.valid && this.orderId) {
      const shippingRequest = {
        ...this.shippingForm.value,
        orderId: this.orderId,
      };

      this.shippingService.create(shippingRequest).subscribe({
        next: (response) => {
          this.dataSource.data = [response, ...this.dataSource.data];
          this.shippingForm.reset({
            shippingMethod: 'STANDARD',
            shippingFee: 0,
            estimatedDeliveryDays: 3,
            trackingNumber: '',
            shippingCompany: '',
            notes: '',
          });
          this.showSuccess('Tạo thông tin vận chuyển thành công');
        },
        error: (error) => {
          this.showError(
            'Lỗi khi tạo thông tin vận chuyển: ' + (error.error?.message || '')
          );
          console.error('Error creating shipping:', error);
        },
      });
    }
  }

  updateStatus(shippingId: number, currentStatus: string): void {
    const dialogRef = this.dialog.open(StatusUpdateDialogComponent, {
      width: '400px',
      data: {
        id: shippingId, // SỬA: đổi từ orderId sang id
        currentStatus: currentStatus,
        type: 'shipping',
        statusOptions: this.statusOptions, // Truyền statusOptions vào dialog
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // SỬA: Cần truyền cả status và notes theo API
        const requestBody = {
          status: result.status,
          notes: result.notes || '',
        };

        this.shippingService.updateStatus(shippingId, requestBody).subscribe({
          next: (updatedShipping) => {
            const index = this.dataSource.data.findIndex(
              (s) => s.id === shippingId
            );
            if (index !== -1) {
              const updatedData = [...this.dataSource.data];
              updatedData[index] = updatedShipping;
              this.dataSource.data = updatedData;
            }
            this.showSuccess('Cập nhật trạng thái thành công');
          },
          error: (error) => {
            this.showError('Lỗi khi cập nhật trạng thái');
            console.error('Error updating shipping status:', error);
          },
        });
      }
    });
  }

  syncWithProvider(shippingId: number): void {
    this.shippingService.syncWithProvider(shippingId).subscribe({
      next: (updatedShipping) => {
        const index = this.dataSource.data.findIndex(
          (s) => s.id === shippingId
        );
        if (index !== -1) {
          const updatedData = [...this.dataSource.data];
          updatedData[index] = updatedShipping;
          this.dataSource.data = updatedData;
        }
        this.showSuccess('Đồng bộ thành công');
      },
      error: (error) => {
        this.showError('Lỗi khi đồng bộ: ' + (error.error?.message || ''));
        console.error('Error syncing shipping:', error);
      },
    });
  }

  getStatusLabel(status: string): string {
    const option = this.statusOptions.find((opt) => opt.value === status);
    return option ? option.label : status;
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'PREPARING':
      case 'READY_TO_SHIP':
        return 'bg-yellow-100 text-yellow-800';
      case 'SHIPPED':
      case 'IN_TRANSIT':
      case 'OUT_FOR_DELIVERY':
        return 'bg-blue-100 text-blue-800';
      case 'DELIVERED':
        return 'bg-green-100 text-green-800';
      case 'CANCELLED':
      case 'RETURNED':
      case 'FAILED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatDate(dateString?: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  formatCurrency(amount?: number): string {
    if (!amount) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(amount);
  }

  editShipping(shipping: ShippingResponse): void {
    // TODO: Implement edit shipping functionality
    console.log('Edit shipping:', shipping);
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Đóng', {
      duration: 3000,
      panelClass: ['success-snackbar'],
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Đóng', {
      duration: 5000,
      panelClass: ['error-snackbar'],
    });
  }

  getColumnLabel(col: string): string {
    const labels: { [key: string]: string } = {
      id: 'Mã',
      trackingNumber: 'Mã theo dõi',
      shippingCompany: 'Đơn vị VC',
      recipientName: 'Người nhận',
      status: 'Trạng thái',
      createdAt: 'Ngày tạo',
      actions: 'Thao tác',
    };
    return labels[col] || col;
  }
}

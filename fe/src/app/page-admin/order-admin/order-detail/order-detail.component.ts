import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  OrderResponse,
  StatusUpdateRequest,
  PaymentUpdateRequest,
  ShippingResponse,
} from 'src/app/api/admin';
import { ShippingAdminControllerService } from 'src/app/api/admin/api/shippingAdminController.service';
import { OrderAdminControllerService } from 'src/app/api/admin/api/orderAdminController.service';
import { StatusUpdateDialogComponent } from '../../../shared/components/status-update-dialog/status-update-dialog.component';

@Component({
  selector: 'app-order-detail',
  templateUrl: './order-detail.component.html',
  styleUrls: ['./order-detail.component.scss'],
})
export class OrderDetailComponent implements OnInit {
  order?: OrderResponse; // Sửa từ OrderEntity thành OrderResponse
  shippingInfo: ShippingResponse[] = [];
  loading = false;
  error = '';

  statusOptions = [
    { value: 'NEW', label: 'Mới' },
    { value: 'CONFIRMED', label: 'Đã xác nhận' },
    { value: 'PROCESSING', label: 'Đang xử lý' },
    { value: 'COMPLETED', label: 'Hoàn thành' },
    { value: 'CANCELLED', label: 'Đã hủy' },
  ];

  paymentStatusOptions = [
    { value: 'PENDING', label: 'Chờ thanh toán' },
    { value: 'PAID', label: 'Đã thanh toán' },
    { value: 'FAILED', label: 'Thanh toán thất bại' },
    { value: 'REFUNDED', label: 'Đã hoàn tiền' },
    { value: 'CANCELLED', label: 'Đã hủy' },
  ];

  shippingStatusOptions = [
    { value: 'PENDING', label: 'Chờ xử lý' },
    { value: 'SHIPPED', label: 'Đã gửi hàng' },
    { value: 'DELIVERED', label: 'Đã giao hàng' },
    { value: 'CANCELLED', label: 'Đã hủy' },
  ];

  constructor(
    private route: ActivatedRoute,
    private orderApi: OrderAdminControllerService,
    private shippingApi: ShippingAdminControllerService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadOrder();
  }

  loadOrder(): void {
    this.loading = true;
    this.error = '';

    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.orderApi.getOrder(id).subscribe({
      next: (res) => {
        this.order = res;
        this.loadShippingInfo();
      },
      error: (err) => {
        console.error('Error loading order:', err);
        this.error = 'Không thể tải thông tin đơn hàng';
        this.loading = false;
      },
    });
  }

  loadShippingInfo(): void {
    if (!this.order?.id) return;

    this.shippingApi.getByOrderId(this.order.id).subscribe({
      next: (res) => {
        this.shippingInfo = res;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading shipping info:', err);
        this.loading = false;
        // Still show order even if shipping info fails
      },
    });
  }

  getStatusLabel(status?: string): string {
    const option = this.statusOptions.find((opt) => opt.value === status);
    return option ? option.label : status || '';
  }

  getPaymentStatusLabel(status?: string): string {
    const option = this.paymentStatusOptions.find(
      (opt) => opt.value === status
    );
    return option ? option.label : status || '';
  }

  getShippingStatusLabel(status?: string): string {
    const option = this.shippingStatusOptions.find(
      (opt) => opt.value === status
    );
    return option ? option.label : status || '';
  }

  openStatusDialog(): void {
    if (!this.order) return;

    const dialogRef = this.dialog.open(StatusUpdateDialogComponent, {
      width: '400px',
      data: {
        orderId: this.order.id,
        currentStatus: this.order.status,
        type: 'order',
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.updateOrderStatus(result.status);
      }
    });
  }

  updateOrderStatus(status: string): void {
    if (!this.order) return;

    const req: StatusUpdateRequest = { status };
    this.orderApi.updateOrderStatus(this.order.id!, req).subscribe({
      next: (res) => {
        this.order = res;
        this.showSuccess('Cập nhật trạng thái thành công');
      },
      error: (err) => {
        console.error('Error updating order status:', err);
        this.showError('Cập nhật trạng thái thất bại');
      },
    });
  }

  updatePaymentStatus(paymentStatus: string): void {
    if (!this.order) return;

    const req: PaymentUpdateRequest = { paymentStatus };
    // Sửa từ updatePayment thành updatePaymentStatus
    this.orderApi.updatePaymentStatus(this.order.id!, req).subscribe({
      next: (res) => {
        this.order = res;
        this.showSuccess('Cập nhật trạng thái thanh toán thành công');
      },
      error: (err) => {
        console.error('Error updating payment status:', err);
        this.showError('Cập nhật trạng thái thanh toán thất bại');
      },
    });
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
}

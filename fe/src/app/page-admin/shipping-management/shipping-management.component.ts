import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

import { 
  OrderAdminControllerService, 
  StatusUpdateRequest,
  StatusUpdateRequestStatusEnum   
} from 'src/app/api/admin';

@Component({
  selector: 'app-shipping-management',
  templateUrl: './shipping-management.component.html',
  styleUrls: ['./shipping-management.component.scss'],
})
export class ShippingManagementComponent implements OnInit {

  orderId!: number;
  trackingCode: string = '';
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderAdminControllerService,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.orderId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.orderId) {
      this.loadOrderInfo();
    }
  }

  loadOrderInfo(): void {
    this.loading = true;
    this.orderService.getOrder(this.orderId).subscribe({
      next: (order: any) => {
        console.log('📋 Order data:', order); // Debug
        this.trackingCode = order.trackingCode || 
                           order.tracking_code || 
                           'Chưa có mã vận đơn';
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.snackBar.open('Không tìm thấy đơn hàng', 'Đóng', { duration: 4000 });
        this.loading = false;
      }
    });
  }

  // ================== BẮT ĐẦU VẬN CHUYỂN ==================
  startShipping(): void {
    if (!this.trackingCode || this.trackingCode === 'Chưa có mã vận đơn') {
      this.snackBar.open('Đơn hàng chưa có mã vận đơn!', 'Đóng', { duration: 4000 });
      return;
    }

    if (!confirm('Xác nhận chuyển đơn hàng sang trạng thái ĐANG VẬN CHUYỂN?')) return;

    this.loading = true;

    const request: StatusUpdateRequest = { 
      status: StatusUpdateRequestStatusEnum.SHIPPING 
    };

    this.orderService.updateOrderStatus(this.orderId, request).subscribe({
      next: () => {
        this.snackBar.open('Đơn hàng đã chuyển sang ĐANG VẬN CHUYỂN!', 'Đóng', { duration: 3000 });
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.snackBar.open('Cập nhật thất bại: ' + (err.error?.message || err.message), 'Đóng', { duration: 5000 });
        this.loading = false;
      }
    });
  }
}
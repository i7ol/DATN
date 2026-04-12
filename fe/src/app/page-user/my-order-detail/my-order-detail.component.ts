import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from 'src/app/shared/services/notification.service';
import { OrderProxyControllerService } from 'src/app/api/user/api/orderProxyController.service';
import { OrderResponse } from 'src/app/api/user/model/orderResponse';

@Component({
  selector: 'my-order-detail',
  templateUrl: './my-order-detail.component.html',
  styleUrls: ['./my-order-detail.component.scss'],
})
export class MyOrderDetailComponent implements OnInit {
  order: OrderResponse | null = null;
  orderId: string = '';
  loading = true;

  // Lưu ảnh được chọn cho từng item
  selectedImages: { [key: string]: string } = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderProxyControllerService,
    private notify: NotificationService,
  ) {}

  ngOnInit(): void {
    this.orderId = this.route.snapshot.paramMap.get('id') || '';

    if (!this.orderId) {
      this.notify.error('Không tìm thấy mã đơn hàng');
      this.router.navigate(['/my-orders']);
      return;
    }

    this.loadOrderDetail();
  }

  private loadOrderDetail(): void {
    this.orderService.getOrder(Number(this.orderId)).subscribe({
      next: (data: OrderResponse) => {
        this.order = data;
        this.initSelectedImages();
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.notify.error('Không tải được thông tin đơn hàng');
        this.loading = false;
      },
    });
  }

  private initSelectedImages(): void {
    if (!this.order?.items) return;

    this.order.items.forEach((item, index) => {
      const key = (item.id || index).toString();
      this.selectedImages[key] = '/assets/img/demo.webp'; // Mặc định
    });
  }

  selectImage(itemKey: string, imageUrl: string): void {
    this.selectedImages[itemKey] = imageUrl;
  }

  getSelectedImage(item: any, index: number): string {
    const key = (item.id || index).toString();
    return this.selectedImages[key] || '/assets/img/demo.webp';
  }

  goBack(): void {
    this.router.navigate(['/my-orders']);
  }

  getStatusClass(status: string | undefined): string {
    if (!status) return 'default';
    switch (status.toUpperCase()) {
      case 'PAID':
      case 'COMPLETED':
        return 'success';
      case 'PENDING':
        return 'warning';
      case 'FAILED':
      case 'CANCELLED':
        return 'error';
      default:
        return 'default';
    }
  }
}

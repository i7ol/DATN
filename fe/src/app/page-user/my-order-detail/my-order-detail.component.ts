import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from 'src/app/shared/services/notification.service';
import { OrderProxyControllerService } from 'src/app/api/user/api/orderProxyController.service';
import { OrderResponse } from 'src/app/api/user/model/orderResponse';
import { OrderItemResponse } from 'src/app/api/admin/model/models';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ReturnCreateComponent } from '../return/return-user/return-user-create.component';
@Component({
  selector: 'my-order-detail',
  templateUrl: './my-order-detail.component.html',
  styleUrls: ['./my-order-detail.component.scss'],
})
export class MyOrderDetailComponent implements OnInit {
  order: OrderResponse | null = null;
  orderId: string = '';
  loading = true;
  daysLeftForReturn: number = 0;
  // Lưu ảnh được chọn cho từng item
  selectedImages: { [key: string]: string } = {};
  selectedReturnItems: OrderItemResponse[] = [];
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderProxyControllerService,
    private notify: NotificationService,
    private modal: NzModalService,
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
      next: (data) => {
        this.order = data;
        this.initSelectedImages();
        this.calculateReturnDeadline();
        this.loading = false;
      },
      error: (err) => {
        this.notify.error('Không tải được đơn hàng');
        this.loading = false;
      },
    });
  }
  canReturn(): boolean {
    return (
      this.order?.status === 'DELIVERED' &&
      !!this.order?.actualDeliveryDate &&
      this.daysLeftForReturn > 0
    );
  }

  // ==================== LOGIC CHỌN SẢN PHẨM ĐỔI TRẢ ====================

  toggleReturnItem(item: OrderItemResponse): void {
    if (!item || !item.id) return;

    const exists = this.selectedReturnItems.some((i) => i.id === item.id);

    if (exists) {
      this.selectedReturnItems = this.selectedReturnItems.filter(
        (i) => i.id !== item.id,
      );
    } else {
      this.selectedReturnItems = [...this.selectedReturnItems, item];
    }

    console.log('SELECTED:', this.selectedReturnItems);
  }
  isSelectedForReturn(item: OrderItemResponse): boolean {
    if (!item || !item.id) return false;

    return this.selectedReturnItems.some((i) => i && i.id === item.id);
  }

  goToCreateReturn(): void {
    if (!this.canReturn()) {
      this.notify.error('Hết thời gian');
      return;
    }

    if (this.selectedReturnItems.length === 0) {
      this.notify.error('Chọn ít nhất 1 sản phẩm');
      return;
    }

    const items = this.selectedReturnItems
      .filter((i) => i && i.id && i.productId) // lọc null
      .map((i) => ({
        orderItemId: i.id!,
        productId: i.productId!,
      }));

    console.log('SELECTED BEFORE SEND:', this.selectedReturnItems);
    const modalRef = this.modal.create({
      nzContent: ReturnCreateComponent,
      nzFooter: null,
      nzWidth: 500,
      nzComponentParams: {
        orderId: this.order?.id,
        items: items,
      },
    });

    modalRef.afterClose.subscribe((result) => {
      if (result?.success) {
        this.selectedReturnItems = []; // reset sau khi tạo thành công
        this.loadOrderDetail();
      }
    });
  }
  getReturnClass(): string {
    if (this.daysLeftForReturn > 3) return 'safe';
    if (this.daysLeftForReturn > 1) return 'warning';
    return 'danger';
  }
  private calculateReturnDeadline(): void {
    if (!this.order?.actualDeliveryDate) return;

    const deliveryDate = new Date(this.order.actualDeliveryDate);
    const deadline = new Date(deliveryDate);
    deadline.setDate(deadline.getDate() + 7);

    const now = new Date();
    const diffTime = deadline.getTime() - now.getTime();

    this.daysLeftForReturn = Math.max(
      0,
      Math.ceil(diffTime / (1000 * 60 * 60 * 24)),
    );
  }
  goToMyReturns(): void {
    this.router.navigate(['/returns']);
  }

  /** Helper an toàn: Lấy URL ảnh (hỗ trợ cả string và object) */
  private getImageUrl(image: any): string {
    if (!image) return '/assets/img/demo.webp';
    if (typeof image === 'string') return image;

    // Trường hợp images là mảng object {url: "..."}
    const obj = image as any;
    return obj?.url || obj?.imageUrl || '/assets/img/demo.webp';
  }

  /** Khởi tạo ảnh đầu tiên từ database */
  private initSelectedImages(): void {
    if (!this.order?.items) return;

    this.order.items.forEach((item, index) => {
      const key = (item.id || index).toString();
      this.selectedImages[key] = this.getImageUrl(item.images?.[0]);
    });
  }

  selectImage(itemKey: string, imageUrl: string): void {
    this.selectedImages[itemKey] = imageUrl;
  }

  getSelectedImage(item: any, index: number): string {
    const key = (item.id || index).toString();
    return (
      this.selectedImages[key] ||
      this.getImageUrl(item.images?.[0]) ||
      '/assets/img/demo.webp'
    );
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

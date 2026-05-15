import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationService } from 'src/app/shared/services/notification.service';
import { OrderProxyControllerService } from 'src/app/api/user/api/orderProxyController.service';
import { OrderResponse } from 'src/app/api/user/model/orderResponse';
import { OrderItemResponse } from 'src/app/api/admin/model/models';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ReturnCreateComponent } from '../return/return-user/return-user-create.component';
import { ReturnUserComponent } from '../return/return-user/return-user.component';
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
    if (!this.order) return false;

    return (
      this.order.status === 'DELIVERED' && !!this.order.actualDeliveryDate
      // Tạm thời bỏ điều kiện > 0 để test
      // && this.daysLeftForReturn > 0
    );
  }

  toggleReturnItem(item: OrderItemResponse): void {
    if (!item) {
      console.warn('Item không hợp lệ:', item);
      return;
    }

    // Quan trọng: Kiểm tra id
    if (!item.id) {
      console.error('Item không có id (orderItemId):', item);
      this.notify.error('Sản phẩm này không thể chọn vì thiếu ID');
      return;
    }

    const index = this.selectedReturnItems.findIndex((i) => i.id === item.id);

    if (index !== -1) {
      // Bỏ chọn
      this.selectedReturnItems = this.selectedReturnItems.filter(
        (_, idx) => idx !== index,
      );
      console.log('❌ Bỏ chọn item:', item.id);
    } else {
      // Thêm vào
      this.selectedReturnItems = [...this.selectedReturnItems, item];
      console.log('✅ Chọn item:', item.id, '-', item.productName);
    }

    console.log('✅ SELECTED ITEMS:', this.selectedReturnItems);
    console.log('✅ Số lượng đã chọn:', this.selectedReturnItems.length);
  }

  isSelectedForReturn(item: OrderItemResponse): boolean {
    if (!item?.id) return false;
    return this.selectedReturnItems.some((i) => i.id === item.id);
  }

  goToCreateReturn(): void {
    console.log('📤 Số item sẽ gửi:', this.selectedReturnItems.length);
    console.log(
      '📦 Selected items chi tiết:',
      this.selectedReturnItems.map((i) => ({
        id: i.id,
        productId: i.productId,
        name: i.productName,
      })),
    );

    if (!this.canReturn()) {
      this.notify.error('Không thể tạo yêu cầu đổi trả lúc này');
      return;
    }

    if (this.selectedReturnItems.length === 0) {
      this.notify.error('Vui lòng chọn ít nhất 1 sản phẩm');
      return;
    }

    // ========== PHẦN SỬA CHÍNH ==========
    const itemsForModal = this.selectedReturnItems
      .filter((i) => i && i.id != null && i.productId != null)
      .map((i) => ({
        orderItemId: Number(i.id), // Đây là field quan trọng nhất
        productId: Number(i.productId),
        quantity: i.quantity || 1,
        reason: '',
      }));

    console.log('📦 Items gửi vào modal (sau mapping):', itemsForModal);

    if (itemsForModal.length === 0) {
      this.notify.error('Không có sản phẩm hợp lệ để tạo yêu cầu đổi trả');
      return;
    }

    const modalRef = this.modal.create({
      nzContent: ReturnCreateComponent,
      nzWidth: 520,
      nzFooter: null,
      nzTitle: null,
      nzClosable: false,
      nzComponentParams: {
        orderId: this.order?.id,
        items: itemsForModal, // Truyền mảng đã map
      },
    });

    modalRef.afterClose.subscribe((result) => {
      if (result?.success) {
        this.selectedReturnItems = []; // Reset sau khi tạo thành công
        this.loadOrderDetail(); // Refresh lại đơn hàng
      }
    });
  }
  getReturnClass(): string {
    if (this.daysLeftForReturn > 3) return 'safe';
    if (this.daysLeftForReturn > 1) return 'warning';
    return 'danger';
  }
  private calculateReturnDeadline(): void {
    if (!this.order?.actualDeliveryDate) {
      this.daysLeftForReturn = 0;
      return;
    }

    const deliveryDate = new Date(this.order.actualDeliveryDate);
    const deadline = new Date(deliveryDate);
    deadline.setDate(deadline.getDate() + 7); // +7 ngày

    const now = new Date();
    const diffTime = deadline.getTime() - now.getTime();

    this.daysLeftForReturn = Math.max(
      0,
      Math.ceil(diffTime / (1000 * 60 * 60 * 24)),
    );

    console.log(`📅 Days left for return: ${this.daysLeftForReturn}`);
  }
  goToMyReturns(): void {
    const modalRef = this.modal.create({
      nzTitle: null,

      nzContent: ReturnUserComponent,

      nzWidth: 1200,

      nzFooter: null,

      nzClosable: true,

      nzCentered: true,

      nzWrapClassName: 'fashion-return-modal',

      nzBodyStyle: {
        padding: '0',
        background: 'transparent',
      },
    });

    modalRef.afterClose.subscribe(() => {
      this.loadOrderDetail();
    });
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

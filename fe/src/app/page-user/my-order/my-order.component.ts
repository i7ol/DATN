import { Component, OnInit } from '@angular/core';
import { AccUserControllerService } from 'src/app/api/user';
import { Pageable } from 'src/app/api/user/model/pageable';
import { OrderResponse } from 'src/app/api/user/model/orderResponse';

@Component({
  selector: 'my-order',
  templateUrl: './my-order.component.html',
  styleUrls: ['./my-order.component.scss'],
})
export class MyOrderComponent implements OnInit {
  orders: OrderResponse[] = [];

  // Phân trang
  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;
  totalElements: number = 0;

  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private accUserService: AccUserControllerService) {}

  ngOnInit(): void {
    this.loadMyOrders(0);
  }

  loadMyOrders(page: number = 0): void {
    this.isLoading = true;
    this.errorMessage = '';

    const pageable: Pageable = {
      page: page,
      size: this.pageSize,
      sort: [],
    };

    this.accUserService.myOrders(pageable).subscribe({
      next: (response: any) => {
        if (response instanceof Blob) {
          response.text().then((text) => {
            const json = JSON.parse(text);
            this.processResponse(json);
          });
        } else {
          this.processResponse(response);
        }
      },
      error: (error: any) => {
        console.error('Lỗi tải đơn hàng:', error);
        this.errorMessage =
          error.status === 401
            ? 'Phiên đăng nhập hết hạn, vui lòng đăng nhập lại.'
            : 'Không thể tải danh sách đơn hàng. Vui lòng thử lại sau.';
        this.isLoading = false;
      },
    });
  }

  private processResponse(response: any): void {
    const result = response?.result || response;
    this.orders = Array.isArray(result?.content) ? result.content : [];

    this.currentPage = result?.number ?? 0;
    this.totalPages = result?.totalPages ?? 0;
    this.totalElements = result?.totalElements ?? 0;

    this.isLoading = false;
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.loadMyOrders(page);
  }

  refresh(): void {
    this.loadMyOrders(this.currentPage);
  }

  // ==================== Helper methods ====================
  getStatusText(status: string | undefined): string {
    if (!status) return 'Không xác định';
    switch (status.toUpperCase()) {
      case 'PENDING':
        return 'Chờ xác nhận';
      case 'PENDING_PAYMENT':
        return 'Chờ thanh toán';
      case 'PROCESSING':
        return 'Đang xử lý';
      case 'SHIPPED':
        return 'Đang giao';
      case 'DELIVERED':
        return 'Đã giao';
      case 'COMPLETED':
        return 'Hoàn thành';
      case 'CANCELLED':
        return 'Đã hủy';
      default:
        return status;
    }
  }

  getStatusClass(status: string | undefined): string {
    if (!status) return 'bg-secondary';
    switch (status.toUpperCase()) {
      case 'PENDING':
      case 'PENDING_PAYMENT':
        return 'bg-warning text-dark';
      case 'PROCESSING':
        return 'bg-info text-white';
      case 'SHIPPED':
        return 'bg-primary text-white';
      case 'DELIVERED':
      case 'COMPLETED':
        return 'bg-success text-white';
      case 'CANCELLED':
        return 'bg-danger text-white';
      default:
        return 'bg-secondary text-white';
    }
  }

  getPageRange(): number[] {
    const range: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    let end = Math.min(this.totalPages - 1, start + maxVisible - 1);

    if (end - start + 1 < maxVisible) {
      start = Math.max(0, end - maxVisible + 1);
    }

    for (let i = start; i <= end; i++) {
      range.push(i);
    }
    return range;
  }

  viewOrderDetail(orderId: number): void {
    console.log('Xem chi tiết đơn hàng #', orderId);
    alert(`Đang xem chi tiết đơn hàng #${orderId}`);
    // TODO: Sau này thay bằng modal đẹp
  }
}

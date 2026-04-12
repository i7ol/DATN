import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
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

  currentPage: number = 0;
  pageSize: number = 10;
  totalPages: number = 0;
  totalElements: number = 0;

  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private accUserService: AccUserControllerService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadMyOrders(0);
  }

  loadMyOrders(page: number = 0): void {
    this.isLoading = true;
    this.errorMessage = '';

    const pageable: Pageable = { page, size: this.pageSize, sort: [] };

    this.accUserService.myOrders(pageable).subscribe({
      next: (response: any) => {
        if (response instanceof Blob) {
          response
            .text()
            .then((text) => this.processResponse(JSON.parse(text)));
        } else {
          this.processResponse(response);
        }
      },
      error: (error) => {
        this.errorMessage =
          error.status === 401
            ? 'Phiên đăng nhập hết hạn, vui lòng đăng nhập lại.'
            : 'Không thể tải danh sách đơn hàng.';
        this.isLoading = false;
      },
    });
  }

  private processResponse(response: any): void {
    const result = response?.result || response;
    this.orders = result?.content || [];
    this.currentPage = result?.number ?? 0;
    this.totalPages = result?.totalPages ?? 0;
    this.totalElements = result?.totalElements ?? 0;
    this.isLoading = false;
  }

  viewOrderDetail(orderId: number): void {
    this.router.navigate(['/my-orders', orderId]);
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.loadMyOrders(page);
  }

  refresh(): void {
    this.loadMyOrders(this.currentPage);
  }

  // Helper
  getStatusText(status: string | undefined): string {
    if (!status) return 'Không xác định';
    const s = status.toUpperCase();
    switch (s) {
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

  getStatusColor(status: string | undefined): string {
    if (!status) return 'default';
    const s = status.toUpperCase();
    if (['PAID', 'COMPLETED', 'DELIVERED'].includes(s)) return 'success';
    if (['PENDING', 'PENDING_PAYMENT'].includes(s)) return 'warning';
    if (['FAILED', 'CANCELLED'].includes(s)) return 'error';
    return 'processing';
  }
}

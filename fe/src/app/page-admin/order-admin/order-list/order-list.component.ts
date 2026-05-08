import { Component, OnInit, AfterViewInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';

import {
  OrderAdminControllerService,
  OrderResponse,
  PageResponseOrderResponse,
  Pageable,
  StatusUpdateRequest,
} from 'src/app/api/admin';

import { StatusUpdateDialogComponent } from 'src/app/shared/components/status-update-dialog/status-update-dialog.component';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss'],
})
export class OrderListComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = [
    'image',
    'id',
    'customer',
    'totalPrice',
    'status',
    'paymentStatus',
    'createdAt',
    'actions',
  ];

  dataSource = new MatTableDataSource<OrderResponse>([]);
  loading = false;

  page = 0;
  size = 10;
  totalElements = 0;

  filterStatus = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private orderService: OrderAdminControllerService,
    private router: Router,
    private dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.dataSource.filterPredicate = (data, filter) => {
      if (!filter) return true;
      return data.status?.toLowerCase() === filter.toLowerCase();
    };
    this.loadOrders();
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
  }

  loadOrders(): void {
    this.loading = true;
    const pageable: Pageable = { page: this.page, size: this.size };

    this.orderService.getAll(pageable).subscribe({
      next: (resp: PageResponseOrderResponse) => {
        this.dataSource.data = resp?.data ?? [];
        this.totalElements = resp?.totalElements ?? 0;
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Load orders failed:', err);
        this.dataSource.data = [];
        this.totalElements = 0;
        this.loading = false;
      },
    });
  }

  getFirstProductImage(order: OrderResponse): string {
    if (!order?.items || order.items.length === 0) {
      return '/assets/img/demo.webp';
    }
    const firstItem = order.items[0];
    const firstImage = firstItem.images?.[0];
    if (!firstImage) return '/assets/img/demo.webp';

    return typeof firstImage === 'string'
      ? firstImage
      : (firstImage as any)?.url ||
          (firstImage as any)?.imageUrl ||
          '/assets/img/demo.webp';
  }

  viewDetail(orderId: number): void {
    this.router.navigate(['/admin/orders', orderId]);
  }

  openStatusDialog(order: OrderResponse): void {
    const dialogRef = this.dialog.open(StatusUpdateDialogComponent, {
      width: '400px',
      data: {
        orderId: order.id,
        currentStatus: order.status,
        type: 'order',
      },
    });

    dialogRef.afterClosed().subscribe((ok) => {
      if (ok) this.loadOrders();
    });
  }

  // ================= MARK AS DELIVERED =================
  markAsDelivered(orderId: number): void {
    if (!confirm('Xác nhận đơn hàng này ĐÃ GIAO HÀNG?')) return;

    this.orderService.markAsDelivered(orderId).subscribe({
      next: () => {
        alert('Đã cập nhật trạng thái ĐÃ GIAO HÀNG!');
        this.loadOrders();
      },
      error: (err) => {
        console.error(err);
        alert('Cập nhật thất bại: ' + (err.error?.message || err.message));
      },
    });
  }

  // ================= MARK AS COMPLETED =================
  markAsCompleted(orderId: number): void {
    if (!confirm('Xác nhận HOÀN THÀNH đơn hàng này?')) return;

    this.orderService.completeOrder(orderId).subscribe({
      next: () => {
        alert('Đơn hàng đã được HOÀN THÀNH!');
        this.loadOrders();
      },
      error: (err) => {
        console.error(err);
        alert('Cập nhật thất bại: ' + (err.error?.message || err.message));
      },
    });
  }

  // ================= HELPERS =================
  getCustomerName(order: OrderResponse): string {
    if (order.userId) return `User #${order.userId}`;
    return order.guestName || 'Khách';
  }

  getStatusClass(status: string | undefined): string {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'PROCESSING':
        return 'bg-blue-100 text-blue-800';
      case 'SHIPPING':
        return 'bg-purple-100 text-purple-800';
      case 'DELIVERED':
        return 'bg-green-100 text-green-800';
      case 'COMPLETED':
        return 'bg-emerald-100 text-emerald-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getPaymentStatusClass(status: string | undefined): string {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'PAID':
        return 'bg-green-100 text-green-800';
      case 'REFUNDED':
        return 'bg-red-100 text-red-800';
      case 'FAILED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  applyFilter(event: Event): void {
    const value = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.dataSource.filter = value;
  }

  filterByStatus(status: string): void {
    this.filterStatus = status;
    this.dataSource.filter = status || '';
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.loadOrders();
  }

  exportOrders(): void {
    console.log('Export orders - Chưa triển khai');
  }
}

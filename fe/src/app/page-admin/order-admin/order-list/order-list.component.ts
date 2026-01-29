import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
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
} from 'src/app/api/admin';

import { StatusUpdateDialogComponent } from 'src/app/shared/components/status-update-dialog/status-update-dialog.component';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss'],
})
export class OrderListComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = [
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

  // paging
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
    this.loadOrders();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  // ================= LOAD ORDERS =================
  loadOrders(): void {
    this.loading = true;

    const pageable: Pageable = {
      page: this.page,
      size: this.size,
    };

    console.log('📤 CALL getAll orders:', pageable);

    this.orderService.getAll(pageable).subscribe({
      next: async (resp: any) => {
        console.log('📥 RAW response:', resp);

        try {
          if (resp instanceof Blob) {
            const text = await resp.text();
            console.log('📄 BLOB text:', text);

            const body: PageResponseOrderResponse = JSON.parse(text);
            this.applyPage(body);
            return;
          }

          if (resp?.body instanceof Blob) {
            const text = await resp.body.text();
            console.log('📄 BLOB body text:', text);

            const body: PageResponseOrderResponse = JSON.parse(text);
            this.applyPage(body);
            return;
          }

          this.applyPage(resp);
        } catch (e) {
          console.error('❌ Parse error:', e);
          this.dataSource.data = [];
          this.totalElements = 0;
        } finally {
          this.loading = false;
        }
      },
      error: (err) => {
        console.error('❌ Load orders failed:', err);
        this.loading = false;
      },
    });
  }
  private applyPage(body?: PageResponseOrderResponse): void {
    console.log(' PARSED body:', body);

    this.dataSource.data = body?.data ?? [];
    this.totalElements = body?.totalElements ?? 0;

    console.log(' SET orders:', this.dataSource.data);
    console.log(' TOTAL:', this.totalElements);
  }

  // ================= ACTIONS =================
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
      if (ok) {
        this.loadOrders();
      }
    });
  }

  // ================= HELPERS =================
  getCustomerName(order: OrderResponse): string {
    if (order.userId) return `User #${order.userId}`;
    return order.guestName || 'Khách';
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'PROCESSING':
        return 'bg-blue-100 text-blue-800';
      case 'SHIPPED':
        return 'bg-purple-100 text-purple-800';
      case 'DELIVERED':
        return 'bg-green-100 text-green-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getPaymentStatusClass(status: string): string {
    switch (status) {
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
    const value = (event.target as HTMLInputElement).value;
    this.dataSource.filter = value.trim().toLowerCase();
  }

  filterByStatus(status: string): void {
    this.filterStatus = status;
    this.dataSource.filter = status || '';
  }

  // ================= PAGINATION =================
  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.loadOrders();
  }

  exportOrders(): void {
    console.log('Export orders');
  }
}

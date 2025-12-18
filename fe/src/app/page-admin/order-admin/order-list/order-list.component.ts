import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { OrderAdminControllerService, OrderResponse } from 'src/app/api/admin'; // Sửa OrderEntity thành OrderResponse
import { StatusUpdateDialogComponent } from 'src/app/shared/components/status-update-dialog/status-update-dialog.component';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.scss'],
})
export class OrderListComponent implements OnInit {
  displayedColumns: string[] = [
    'id',
    'customer',
    'totalPrice',
    'status',
    'paymentStatus',
    'createdAt',
    'actions',
  ];
  dataSource = new MatTableDataSource<OrderResponse>(); // Sửa từ OrderEntity thành OrderResponse
  loading = false;
  filterStatus = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private orderService: OrderAdminControllerService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadOrders(): void {
    this.loading = true;
    this.orderService.getAllOrders().subscribe({
      next: (orders) => {
        this.dataSource.data = orders;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.loading = false;
      },
    });
  }

  viewDetail(orderId: number): void {
    this.router.navigate(['/admin/orders', orderId]);
  }

  openStatusDialog(order: OrderResponse): void {
    // Sửa từ OrderEntity thành OrderResponse
    const dialogRef = this.dialog.open(StatusUpdateDialogComponent, {
      width: '400px',
      data: {
        orderId: order.id,
        currentStatus: order.status,
        type: 'order',
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.loadOrders();
      }
    });
  }

  getCustomerName(order: OrderResponse): string {
    // Sửa từ OrderEntity thành OrderResponse
    return order.userId ? `User #${order.userId}` : order.guestName || 'Khách';
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
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  filterByStatus(status: string): void {
    this.filterStatus = status;
    this.dataSource.filter = status || '';
  }

  exportOrders(): void {
    // Implement export logic
    console.log('Export orders');
  }
}

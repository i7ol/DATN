import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PaymentAdminControllerService } from 'src/app/api/admin/api/paymentAdminController.service';
import { Pageable } from 'src/app/api/admin';
import { SimpleDialogComponent } from 'src/app/shared/components/simple-dialog/simple-dialog.component';
// Định nghĩa các interface cục bộ vì chưa có trong module
interface PaymentResponse {
  id?: number;
  orderId?: number;
  method?: string;
  amount?: number;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
  paymentMethod?: string;
}

interface PaymentSearchRequest {
  status?: string;
  method?: string;
  fromDate?: string;
  toDate?: string;
  minAmount?: number;
  maxAmount?: number;
}

@Component({
  selector: 'app-payment-list',
  templateUrl: './payment-list.component.html',
  styleUrls: ['./payment-list.component.scss'],
})
export class PaymentListComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = [
    'id',
    'orderId',
    'method',
    'amount',
    'status',
    'createdAt',
    'updatedAt',
    'actions',
  ];

  dataSource = new MatTableDataSource<PaymentResponse>();
  payments: PaymentResponse[] = [];
  isLoading = false;
  totalItems = 0;
  pageSize = 10;
  pageIndex = 0;
  Math = Math;

  paymentStatusOptions = [
    { value: 'PENDING', label: 'Chờ thanh toán' },
    { value: 'PAID', label: 'Đã thanh toán' },
    { value: 'REFUNDED', label: 'Đã hoàn tiền' },
    { value: 'FAILED', label: 'Thanh toán thất bại' },
    { value: 'CANCELLED', label: 'Đã hủy' },
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private paymentService: PaymentAdminControllerService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadPayments();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadPayments(): void {
    this.isLoading = true;

    // Tạo pageable đúng format
    const pageable: Pageable = {
      page: this.pageIndex,
      size: this.pageSize,
      sort: ['createdAt,desc'],
    };

    // Gọi API với các tham số đúng
    this.paymentService
      .getAllPayments(
        pageable,
        undefined, // status
        undefined, // method
        undefined, // fromDate
        undefined, // toDate
        undefined, // minAmount
        undefined // maxAmount
      )
      .subscribe({
        next: (response: any) => {
          // Xử lý response dạng object
          if (response && response.content) {
            this.payments = response.content || [];
            this.dataSource.data = this.payments;
            this.totalItems = response.totalElements || 0;
          } else {
            // Nếu response là array trực tiếp
            this.payments = response || [];
            this.dataSource.data = this.payments;
            this.totalItems = this.payments.length;
          }
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading payments:', error);
          this.showError('Không thể tải danh sách thanh toán');
          this.isLoading = false;
        },
      });
  }

  // Thêm các phương thức để tính toán
  getTotalCount(): number {
    return this.payments.length;
  }

  getPaidCount(): number {
    return this.payments.filter((p) => p.status === 'PAID').length;
  }

  getPendingCount(): number {
    return this.payments.filter((p) => p.status === 'PENDING').length;
  }

  getFailedCount(): number {
    return this.payments.filter(
      (p) => p.status === 'FAILED' || p.status === 'REFUNDED'
    ).length;
  }

  getDisplayedTo(): number {
    return Math.min((this.pageIndex + 1) * this.pageSize, this.totalItems);
  }

  getStatusLabel(status: string | undefined | null): string {
    if (!status) return 'N/A';
    const option = this.paymentStatusOptions.find(
      (opt) => opt.value === status
    );
    return option ? option.label : status;
  }

  getStatusColor(status: string | undefined | null): string {
    const statusValue = status || '';
    switch (statusValue) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'PAID':
        return 'bg-green-100 text-green-800';
      case 'REFUNDED':
        return 'bg-blue-100 text-blue-800';
      case 'FAILED':
        return 'bg-red-100 text-red-800';
      case 'CANCELLED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  openUpdateDialog(payment: PaymentResponse): void {
    // Tạo component dialog đơn giản nếu chưa có
    const dialogRef = this.dialog.open(SimpleDialogComponent, {
      width: '500px',
      data: {
        title: 'Cập nhật thanh toán',
        message: 'Chức năng đang được phát triển',
        type: 'info',
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'reload') {
        this.loadPayments();
      }
    });
  }

  markAsPaid(payment: PaymentResponse): void {
    if (!payment.id) return;

    const transactionId = `ADMIN_${new Date().getTime()}`;

    this.paymentService.markPaid(payment.id, transactionId).subscribe({
      next: () => {
        this.showSuccess('Đã đánh dấu đã thanh toán thành công');
        this.loadPayments();
      },
      error: (error) => {
        console.error('Error marking as paid:', error);
        this.showError('Không thể cập nhật trạng thái thanh toán');
      },
    });
  }

  refundPayment(payment: PaymentResponse): void {
    if (!payment.id) return;

    const requestBody = {
      refundReason: 'Hoàn tiền thủ công bởi admin',
    };

    this.paymentService.refund(payment.id, requestBody).subscribe({
      next: () => {
        this.showSuccess('Đã hoàn tiền thành công');
        this.loadPayments();
      },
      error: (error) => {
        console.error('Error refunding:', error);
        this.showError('Không thể hoàn tiền');
      },
    });
  }

  viewOrder(orderId: number): void {
    this.router.navigate(['/admin/orders', orderId]);
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  applyStatusFilter(status: string): void {
    if (!status) {
      this.dataSource.filter = '';
    } else {
      this.dataSource.filter = status;
    }

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  formatCurrency(amount: number | undefined): string {
    if (!amount) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(amount);
  }

  getColumnLabel(col: string): string {
    const labels: { [key: string]: string } = {
      id: 'Mã',
      orderId: 'Đơn hàng',
      method: 'Phương thức',
      amount: 'Số tiền',
      status: 'Trạng thái',
      createdAt: 'Ngày tạo',
      updatedAt: 'Cập nhật',
      actions: 'Thao tác',
    };
    return labels[col] || col;
  }

  formatDate(date: string | null | undefined): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPayments();
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Đóng', {
      duration: 3000,
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Đóng', {
      duration: 5000,
    });
  }
}

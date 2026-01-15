import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

import { PaymentAdminProxyControllerService } from 'src/app/api/admin/api/paymentAdminProxyController.service';
import { Pageable } from 'src/app/api/admin';
import { SimpleDialogComponent } from 'src/app/shared/components/simple-dialog/simple-dialog.component';

import {
  PaymentResponse,
  PaymentResponseStatusEnum,
} from 'src/app/api/admin/model/paymentResponse';
import { PaymentSearchRequest } from 'src/app/api/admin/model/paymentSearchRequest';

@Component({
  selector: 'app-payment-list',
  templateUrl: './payment-list.component.html',
  styleUrls: ['./payment-list.component.scss'],
})
export class PaymentListComponent implements OnInit, AfterViewInit {
  /* ================= TABLE ================= */
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

  /* ================= STATE ================= */
  isLoading = false;
  totalItems = 0;
  pageSize = 10;
  pageIndex = 0;

  /* ================= FILTER ================= */
  currentFilter: PaymentSearchRequest = {};

  paymentStatusOptions = [
    { value: PaymentResponseStatusEnum.PENDING, label: 'Chờ thanh toán' },
    { value: PaymentResponseStatusEnum.PAID, label: 'Đã thanh toán' },
    { value: PaymentResponseStatusEnum.REFUNDED, label: 'Đã hoàn tiền' },
    { value: PaymentResponseStatusEnum.FAILED, label: 'Thanh toán thất bại' },
    { value: PaymentResponseStatusEnum.CANCELLED, label: 'Đã hủy' },
  ];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private paymentService: PaymentAdminProxyControllerService,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  /* ================= LIFECYCLE ================= */
  ngOnInit(): void {
    this.loadPayments();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  /* ================= LOAD DATA ================= */
  loadPayments(filter: PaymentSearchRequest = this.currentFilter): void {
    this.isLoading = true;

    const pageable: Pageable = {
      page: this.pageIndex,
      size: this.pageSize,
      sort: ['createdAt,desc'],
    };

    this.paymentService.getAllPayments(filter, pageable).subscribe({
      next: (res) => {
        this.payments = res.content || [];
        this.dataSource.data = this.payments;
        this.totalItems = res.totalElements || 0;
        this.isLoading = false;
      },
      error: () => {
        this.showError('Không thể tải danh sách thanh toán');
        this.isLoading = false;
      },
    });
  }

  /* ================= FILTER ================= */
  applyStatusFilter(status?: PaymentResponseStatusEnum): void {
    this.pageIndex = 0;
    this.currentFilter = status ? { status } : {};
    this.loadPayments();
  }

  /* ================= ACTIONS ================= */
  markAsPaid(payment: PaymentResponse): void {
    if (!payment.id) return;

    const transactionId = `ADMIN_${Date.now()}`;

    this.paymentService.markPaid(payment.id, transactionId).subscribe({
      next: () => {
        this.showSuccess('Đã đánh dấu thanh toán thành công');
        this.loadPayments();
      },
      error: () => {
        this.showError('Không thể cập nhật trạng thái');
      },
    });
  }

  refundPayment(payment: PaymentResponse): void {
    if (!payment.id) return;

    this.paymentService
      .refund(payment.id, { refundReason: 'Hoàn tiền thủ công bởi admin' })
      .subscribe({
        next: () => {
          this.showSuccess('Hoàn tiền thành công');
          this.loadPayments();
        },
        error: () => {
          this.showError('Không thể hoàn tiền');
        },
      });
  }

  viewOrder(orderId: number): void {
    this.router.navigate(['/admin/orders', orderId]);
  }

  /* ================= PAGINATION ================= */
  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPayments();
  }

  /* ================= UI HELPERS ================= */
  getStatusLabel(status?: PaymentResponseStatusEnum | null): string {
    if (!status) return 'N/A';
    const found = this.paymentStatusOptions.find((s) => s.value === status);
    return found ? found.label : status;
  }

  getStatusColor(status?: PaymentResponseStatusEnum | null): string {
    switch (status) {
      case PaymentResponseStatusEnum.PENDING:
        return 'bg-yellow-100 text-yellow-800';
      case PaymentResponseStatusEnum.PAID:
        return 'bg-green-100 text-green-800';
      case PaymentResponseStatusEnum.REFUNDED:
        return 'bg-blue-100 text-blue-800';
      case PaymentResponseStatusEnum.FAILED:
        return 'bg-red-100 text-red-800';
      case PaymentResponseStatusEnum.CANCELLED:
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatCurrency(amount?: number): string {
    if (!amount) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(amount);
  }

  formatDate(date?: string | null): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  /* ================= NOTIFY ================= */
  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Đóng', { duration: 3000 });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Đóng', { duration: 5000 });
  }
  /* ================= STATS ================= */
  getTotalCount(): number {
    return this.totalItems;
  }

  getPaidCount(): number {
    return this.payments.filter(
      (p) => p.status === PaymentResponseStatusEnum.PAID
    ).length;
  }

  getPendingCount(): number {
    return this.payments.filter(
      (p) => p.status === PaymentResponseStatusEnum.PENDING
    ).length;
  }

  getFailedCount(): number {
    return this.payments.filter(
      (p) =>
        p.status === PaymentResponseStatusEnum.FAILED ||
        p.status === PaymentResponseStatusEnum.REFUNDED
    ).length;
  }

  /* ================= SEARCH ================= */
  applyFilter(event: Event): void {
    const value = (event.target as HTMLInputElement).value.trim();

    this.pageIndex = 0;

    this.currentFilter = value
      ? {
          transactionId: value,
        }
      : {};

    this.loadPayments(this.currentFilter);
  }

  /* ================= TABLE ================= */
  getColumnLabel(col: string): string {
    const map: Record<string, string> = {
      id: 'ID',
      orderId: 'Mã đơn',
      method: 'Phương thức',
      amount: 'Số tiền',
      status: 'Trạng thái',
      createdAt: 'Ngày tạo',
      updatedAt: 'Cập nhật',
      actions: 'Thao tác',
    };
    return map[col] || col;
  }

  /* ================= PAGINATION ================= */
  getDisplayedTo(): number {
    const to = (this.pageIndex + 1) * this.pageSize;
    return to > this.totalItems ? this.totalItems : to;
  }

  /* ================= DIALOG ================= */
  openUpdateDialog(payment?: PaymentResponse): void {
    this.dialog.open(SimpleDialogComponent, {
      width: '500px',
      data: {
        title: 'Cập nhật thanh toán',
        message: payment
          ? `Thanh toán #${payment.id}`
          : 'Chức năng đang được phát triển',
        type: 'info',
      },
    });
  }
}

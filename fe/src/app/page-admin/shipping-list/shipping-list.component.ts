import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { Router } from '@angular/router';
import {
  ShippingResponse,
  ShippingSearchRequest,
  Pageable,
  PageShippingResponse,
  ShippingSearchRequestStatusEnum,
} from 'src/app/api/admin';
import { ShippingAdminControllerService } from 'src/app/api/admin/api/shippingAdminController.service';
@Component({
  selector: 'app-shipping-list',
  templateUrl: './shipping-list.component.html',
  styleUrls: ['./shipping-list.component.scss'],
})
export class ShippingListComponent implements OnInit {
  displayedColumns: string[] = [
    'id',
    'orderId',
    'recipientName',
    'shippingCompany',
    'trackingNumber',
    'status',
    'createdAt',
    'actions',
  ];
  dataSource = new MatTableDataSource<ShippingResponse>();
  loading = false;
  pageSize = 10;
  pageIndex = 0;
  totalItems = 0;
  totalPages = 0;

  // Filter states - SỬA: Dùng ShippingSearchRequestStatusEnum | undefined
  statusFilter: ShippingSearchRequestStatusEnum | undefined = undefined;
  companyFilter = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  // Status options cho dropdown filter
  statusOptions = [
    {
      value: ShippingSearchRequestStatusEnum.PREPARING,
      label: 'Đang chuẩn bị',
    },
    {
      value: ShippingSearchRequestStatusEnum.READY_TO_SHIP,
      label: 'Sẵn sàng giao',
    },
    { value: ShippingSearchRequestStatusEnum.SHIPPED, label: 'Đã gửi hàng' },
    {
      value: ShippingSearchRequestStatusEnum.IN_TRANSIT,
      label: 'Đang vận chuyển',
    },
    {
      value: ShippingSearchRequestStatusEnum.OUT_FOR_DELIVERY,
      label: 'Đang giao hàng',
    },
    { value: ShippingSearchRequestStatusEnum.DELIVERED, label: 'Đã giao hàng' },
    { value: ShippingSearchRequestStatusEnum.CANCELLED, label: 'Đã hủy' },
    { value: ShippingSearchRequestStatusEnum.RETURNED, label: 'Đã trả hàng' },
    {
      value: ShippingSearchRequestStatusEnum.FAILED,
      label: 'Giao hàng thất bại',
    },
  ];

  constructor(
    private shippingService: ShippingAdminControllerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadShippings();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadShippings(): void {
    this.loading = true;

    // Tạo filter và pageable đúng với API
    const filter: ShippingSearchRequest = {
      status: this.statusFilter || undefined,
      shippingCompany: this.companyFilter || undefined,
    };

    const pageable: Pageable = {
      page: this.pageIndex,
      size: this.pageSize,
      sort: ['createdAt,desc'],
    };

    this.shippingService.getAllShippings(filter, pageable).subscribe({
      next: (response: PageShippingResponse) => {
        this.dataSource.data = response.content || [];
        this.totalItems = response.totalElements || 0;
        this.totalPages = response.totalPages || 0;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading shippings:', error);
        this.loading = false;
      },
    });
  }

  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadShippings();
  }

  applyStatusFilter(statusValue: string): void {
    // SỬA: Convert string sang ShippingSearchRequestStatusEnum
    if (statusValue === '') {
      this.statusFilter = undefined;
    } else {
      this.statusFilter = statusValue as ShippingSearchRequestStatusEnum;
    }
    this.pageIndex = 0;
    this.loadShippings();
  }

  applyCompanyFilter(): void {
    this.pageIndex = 0;
    this.loadShippings();
  }

  resetFilters(): void {
    this.statusFilter = undefined;
    this.companyFilter = '';
    this.pageIndex = 0;
    this.loadShippings();
  }

  viewDetail(shippingId: number): void {
    this.router.navigate(['/admin/shippings', shippingId]);
  }

  viewOrder(orderId: number): void {
    this.router.navigate(['/admin/orders', orderId]);
  }

  getStatusClass(status: string): string {
    // SỬA: Nhận string vì từ API trả về string
    switch (status) {
      case 'PREPARING':
        return 'bg-yellow-100 text-yellow-800';
      case 'READY_TO_SHIP':
        return 'bg-orange-100 text-orange-800';
      case 'SHIPPED':
        return 'bg-blue-100 text-blue-800';
      case 'IN_TRANSIT':
        return 'bg-indigo-100 text-indigo-800';
      case 'OUT_FOR_DELIVERY':
        return 'bg-purple-100 text-purple-800';
      case 'DELIVERED':
        return 'bg-green-100 text-green-800';
      case 'CANCELLED':
      case 'RETURNED':
      case 'FAILED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  getStatusLabel(status: string): string {
    // SỬA: Nhận string và tìm trong options
    const option = this.statusOptions.find((opt) => opt.value === status);
    return option ? option.label : status;
  }

  syncWithProvider(shippingId: number): void {
    this.shippingService.syncWithProvider(shippingId).subscribe({
      next: () => {
        alert('Đồng bộ thành công!');
        this.loadShippings();
      },
      error: (error) => {
        console.error('Error syncing shipping:', error);
        alert(
          'Đồng bộ thất bại: ' + (error.error?.message || 'Vui lòng thử lại')
        );
      },
    });
  }

  formatCurrency(amount?: number): string {
    if (!amount) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(amount);
  }

  formatDate(date?: string): string {
    if (!date) return 'N/A';
    return new Date(date).toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}

import { ShippingAdminControllerService } from 'src/app/api/admin/api/shippingAdminController.service';
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

@Component({
  selector: 'app-shipping-list',
  templateUrl: './shipping-list.component.html',
  styleUrls: ['./shipping-list.component.scss'],
})
export class ShippingListComponent implements OnInit {
  displayedColumns = [
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

  statusFilter?: ShippingSearchRequestStatusEnum;
  companyFilter = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  statusOptions = Object.values(ShippingSearchRequestStatusEnum).map((v) => ({
    value: v,
    label: v,
  }));

  constructor(
    private shippingService: ShippingAdminControllerService,
    private router: Router,
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

    const filter: ShippingSearchRequest = {
      status: this.statusFilter,
      shippingCompany: this.companyFilter || undefined,
    };

    const pageable: Pageable = {
      page: this.pageIndex,
      size: this.pageSize,
    };

    this.shippingService.getAllShippings(filter, pageable).subscribe({
      next: async (res) => {
        const page = await parseBlobJson<PageShippingResponse>(res);
        this.dataSource.data = page.content || [];
        this.totalItems = page.totalElements || 0;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  onPageChange(e: any): void {
    this.pageIndex = e.pageIndex;
    this.pageSize = e.pageSize;
    this.loadShippings();
  }

  resetFilters(): void {
    this.statusFilter = undefined;
    this.companyFilter = '';
    this.pageIndex = 0;
    this.loadShippings();
  }

  viewDetail(id: number): void {
    this.router.navigate(['/admin/shippings', id]);
  }

  viewOrder(orderId: number): void {
    this.router.navigate(['/admin/orders', orderId]);
  }

  applyStatusFilter(value: ShippingSearchRequestStatusEnum | string): void {
    this.statusFilter = value as ShippingSearchRequestStatusEnum;
    this.pageIndex = 0;
    this.loadShippings();
  }

  applyCompanyFilter(): void {
    this.pageIndex = 0;
    this.loadShippings();
  }
}

/* ====== parseBlobJson ====== */
export function parseBlobJson<T>(body: any): Promise<T> {
  if (body instanceof Blob) {
    return body.text().then((text) => JSON.parse(text) as T);
  }
  return Promise.resolve(body as T);
}

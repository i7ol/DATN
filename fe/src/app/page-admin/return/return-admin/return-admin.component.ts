import { Component, OnInit } from '@angular/core';
import { ReturnAdminProxyControllerService } from 'src/app/api/admin/api/returnAdminProxyController.service';
import { ReturnResponse } from 'src/app/api/admin/model/returnResponse';

@Component({
  selector: 'app-return-admin',
  templateUrl: './return-admin.component.html',
  styleUrls: ['./return-admin.component.scss'],
})
export class ReturnAdminComponent implements OnInit {
  returns: ReturnResponse[] = [];
  currentPage = 0;
  pageSize = 10;
  total = 0;

  tab = 'pending'; // 'pending' | 'all'

  constructor(private returnAdminService: ReturnAdminProxyControllerService) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    const pageable = { page: this.currentPage, size: this.pageSize };

    const obs =
      this.tab === 'pending'
        ? this.returnAdminService.getPendingReturns(pageable)
        : this.returnAdminService.getAllReturns(pageable);

    obs.subscribe({
      next: (res: any) => {
        console.log('📦 API Response:', res); // Giữ lại để debug

        this.returns = res?.data || []; // ← Sửa thành 'data'
        this.total = res?.totalElements || 0; // ← Sửa thành totalElements
      },
      error: (err) => {
        console.error('Load returns error:', err);
      },
    });
  }

  changeTab(tab: string) {
    this.tab = tab;
    this.currentPage = 0;
    this.loadData();
  }

  onPageChange(page: number) {
    this.currentPage = page - 1;
    this.loadData();
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'orange';
      case 'APPROVED':
        return 'green';
      case 'REJECTED':
        return 'red';
      case 'COMPLETED':
        return 'blue';
      default:
        return 'default';
    }
  }
}

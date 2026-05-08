import { Component, OnInit } from '@angular/core';
import { PageResponseReturnResponse } from 'src/app/api/admin/model/models';
import { ReturnAdminService } from './return-admin.service';

@Component({
  selector: 'app-return-admin',
  templateUrl: './return-admin.component.html',
  styleUrls: ['./return-admin.component.scss'],
})
export class ReturnAdminComponent implements OnInit {
  returns: any[] = [];
  currentPage = 0;
  pageSize = 10;
  total = 0;

  tab = 'pending'; // 'pending' | 'all'

  constructor(private adminService: ReturnAdminService) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    const pageable = {
      page: this.currentPage,
      size: this.pageSize,
    };

    const obs =
      this.tab === 'pending'
        ? this.adminService.getPendingReturns(pageable)
        : this.adminService.getAllReturns(pageable);

    obs.subscribe((res: PageResponseReturnResponse) => {
      const data = (res as any).result;

      this.returns = data?.content || [];
      this.total = data?.totalElements || 0;
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

  // ✅ FIX lỗi ngClass bằng function
  getStatusClass(status: string): string {
    switch (status) {
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

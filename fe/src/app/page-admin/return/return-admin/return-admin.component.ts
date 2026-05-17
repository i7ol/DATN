import { Component, OnInit } from '@angular/core';
import { NzModalService } from 'ng-zorro-antd/modal';
import { ReturnAdminProxyControllerService } from 'src/app/api/admin/api/returnAdminProxyController.service';
import { ReturnResponse } from 'src/app/api/admin/model/returnResponse';
import { ReturnDetailAdmin } from './return-detail-admin';
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

  tab = 'pending';

  constructor(
    private returnAdminService: ReturnAdminProxyControllerService,
    private modal: NzModalService,
  ) {}

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
        this.returns = res?.data || [];
        this.total = res?.totalElements || 0;
      },
      error: (err) => console.error(err),
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

  openDetail(returnId: number) {
    const modalRef = this.modal.create({
      nzTitle: '',

      nzContent: ReturnDetailAdmin,

      nzWidth: 900, // tăng width

      nzFooter: null,

      nzClosable: true,

      nzCentered: true,

      nzWrapClassName: 'return-detail-modal',

      nzBodyStyle: {
        padding: '0',
        overflow: 'hidden',
        borderRadius: '24px',
      },

      nzComponentParams: {
        returnId: returnId,
      },
    });

    modalRef.afterClose.subscribe(() => {
      this.loadData();
    });
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

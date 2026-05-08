import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReturnAdminService } from '../return-admin/return-admin.service';
import { ReturnResponse } from 'src/app/api/admin/model/models';

@Component({
  selector: 'app-return-detail-admin',
  templateUrl: './return-detail-admin.component.html',
})
export class ReturnDetailAdminComponent implements OnInit {
  returnDetail?: ReturnResponse;
  returnId!: number;

  constructor(
    private route: ActivatedRoute,
    private adminService: ReturnAdminService,
  ) {}

  ngOnInit() {
    this.returnId = +this.route.snapshot.paramMap.get('id')!;
    this.loadDetail();
  }

  loadDetail() {
    this.adminService.getReturnDetail(this.returnId).subscribe((res: any) => {
      this.returnDetail = res.result || res;
    });
  }

  // ✅ FIX lỗi ngClass
  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'badge bg-warning';
      case 'APPROVED':
        return 'badge bg-success';
      case 'REJECTED':
        return 'badge bg-danger';
      case 'COMPLETED':
        return 'badge bg-secondary';
      default:
        return 'badge bg-dark';
    }
  }

  approve(refundAmount: any, adminNote: string) {
    if (!refundAmount) {
      alert('Vui lòng nhập số tiền hoàn');
      return;
    }

    this.adminService
      .approveReturn(this.returnId, Number(refundAmount), adminNote)
      .subscribe(() => {
        alert('Đã phê duyệt!');
        this.loadDetail();
      });
  }

  reject(adminNote: string) {
    if (!adminNote) {
      alert('Vui lòng nhập lý do từ chối');
      return;
    }

    this.adminService.rejectReturn(this.returnId, adminNote).subscribe(() => {
      alert('Đã từ chối!');
      this.loadDetail();
    });
  }

  complete(refundTransactionId: string) {
    if (!refundTransactionId) {
      alert('Vui lòng nhập mã giao dịch');
      return;
    }

    this.adminService
      .completeReturn(this.returnId, refundTransactionId)
      .subscribe(() => {
        alert('Hoàn tất!');
        this.loadDetail();
      });
  }
}

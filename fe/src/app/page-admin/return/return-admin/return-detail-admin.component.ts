import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReturnAdminProxyControllerService } from 'src/app/api/admin/api/returnAdminProxyController.service';
import { ReturnResponse } from 'src/app/api/admin/model/returnResponse';

@Component({
  selector: 'app-return-detail-admin',
  templateUrl: './return-detail-admin.component.html',
})
export class ReturnDetailAdminComponent implements OnInit {
  returnDetail?: ReturnResponse;
  returnId!: number;

  constructor(
    private route: ActivatedRoute,
    private returnAdminService: ReturnAdminProxyControllerService,
  ) {}

  ngOnInit() {
    this.returnId = +this.route.snapshot.paramMap.get('id')!;
    this.loadDetail();
  }

  loadDetail() {
    this.returnAdminService.getReturnDetail(this.returnId).subscribe({
      next: (res: any) => {
        console.log('📦 Return Detail Response:', res);

        this.returnDetail = res?.result || res;
      },
      error: (err) => console.error(err),
    });
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'badge bg-warning';
      case 'APPROVED':
        return 'badge bg-success';
      case 'REJECTED':
        return 'badge bg-danger';
      case 'COMPLETED':
        return 'badge bg-info';
      default:
        return 'badge bg-dark';
    }
  }

  approve(refundAmount: string, adminNote: string) {
    if (!refundAmount) {
      alert('Vui lòng nhập số tiền hoàn');
      return;
    }

    this.returnAdminService
      .approveReturn(this.returnId, Number(refundAmount), adminNote)
      .subscribe(() => {
        alert('Đã phê duyệt thành công!');
        this.loadDetail();
      });
  }

  reject(adminNote: string) {
    if (!adminNote) {
      alert('Vui lòng nhập lý do từ chối');
      return;
    }

    this.returnAdminService
      .rejectReturn(this.returnId, adminNote)
      .subscribe(() => {
        alert('Đã từ chối!');
        this.loadDetail();
      });
  }

  complete(refundTransactionId: string) {
    if (!refundTransactionId) {
      alert('Vui lòng nhập mã giao dịch hoàn tiền');
      return;
    }

    this.returnAdminService
      .completeReturn(this.returnId, refundTransactionId)
      .subscribe(() => {
        alert('Hoàn tất đơn đổi trả!');
        this.loadDetail();
      });
  }
}

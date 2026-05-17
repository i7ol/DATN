import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReturnAdminProxyControllerService } from 'src/app/api/admin/api/returnAdminProxyController.service';
import { ReturnResponse } from 'src/app/api/admin/model/returnResponse';

@Component({
  selector: 'app-return-detail-admin',
  templateUrl: './return-detail-admin.component.html',
  styleUrls: ['./return-detail-admin.component.scss'],
})
export class ReturnDetailAdmin implements OnInit {
  @Input() returnId!: number;

  returnDetail?: ReturnResponse;

  constructor(private returnAdminService: ReturnAdminProxyControllerService) {}

  ngOnInit() {
    this.loadDetail();
  }

  loadDetail() {
    this.returnAdminService.getReturnDetail(this.returnId).subscribe({
      next: (res: any) => {
        this.returnDetail = res?.result || res;
      },
      error: (err) => console.error(err),
    });
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'status-pending';

      case 'APPROVED':
        return 'status-approved';

      case 'REJECTED':
        return 'status-rejected';

      case 'COMPLETED':
        return 'status-completed';

      default:
        return '';
    }
  }

  approve(refundAmount: string, adminNote: string) {
    if (!refundAmount) return alert('Vui lòng nhập số tiền hoàn');

    this.returnAdminService
      .approveReturn(this.returnId, Number(refundAmount), adminNote)
      .subscribe(() => {
        alert('Đã phê duyệt!');
        this.loadDetail();
      });
  }

  reject(adminNote: string) {
    if (!adminNote) return alert('Vui lòng nhập lý do từ chối');

    this.returnAdminService
      .rejectReturn(this.returnId, adminNote)
      .subscribe(() => {
        alert('Đã từ chối!');
        this.loadDetail();
      });
  }

  complete(refundTransactionId: string) {
    if (!refundTransactionId) return alert('Vui lòng nhập mã giao dịch');

    this.returnAdminService
      .completeReturn(this.returnId, refundTransactionId)
      .subscribe(() => {
        alert('Hoàn tất!');
        this.loadDetail();
      });
  }
}

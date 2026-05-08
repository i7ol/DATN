import { Injectable } from '@angular/core';
import { ReturnAdminProxyControllerService } from 'src/app/api/admin';
import { PageResponseReturnResponse } from 'src/app/api/admin/model/pageResponseReturnResponse';
import { ReturnResponse } from 'src/app/api/admin/model/returnResponse';
@Injectable({
  providedIn: 'root',
})
export class ReturnAdminService {
  constructor(private api: ReturnAdminProxyControllerService) {}

  getAllReturns(pageable: any) {
    return this.api.getAllReturns(pageable);
  }

  getPendingReturns(pageable: any) {
    return this.api.getPendingReturns(pageable);
  }

  getReturnDetail(returnId: number) {
    return this.api.getReturnDetail(returnId);
  }

  approveReturn(returnId: number, refundAmount: number, adminNote?: string) {
    return this.api.approveReturn(returnId, refundAmount, adminNote);
  }

  rejectReturn(returnId: number, adminNote: string) {
    return this.api.rejectReturn(returnId, adminNote);
  }

  completeReturn(returnId: number, refundTransactionId?: string) {
    return this.api.completeReturn(returnId, refundTransactionId);
  }
}

import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NzModalRef, NzModalService } from 'ng-zorro-antd/modal';
import { ReturnProxyControllerService } from 'src/app/api/user';

import {
  CreateReturnRequest,
  ReturnItemRequest,
} from 'src/app/api/user/model/models';
import { NotificationService } from 'src/app/shared/services/notification.service';

@Component({
  selector: 'app-return-create',
  templateUrl: './return-user-create.component.html',
  styleUrls: ['./return-user-create.component.scss'],
})
export class ReturnCreateComponent implements OnInit {
  @Input() orderId!: number;
  @Input() items: { orderItemId: number; productId: number }[] = [];
  form!: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,

    private modalRef: NzModalRef,
    private modal: NzModalService,
    private returnService: ReturnProxyControllerService,
    private notify: NotificationService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      returnType: ['WARRANTY', Validators.required],
      reason: ['', Validators.required],
      description: [''],
    });
  }

  submit(): void {
    if (this.form.invalid || !this.orderId) {
      this.notify.error('Thiếu thông tin đơn hàng');
      return;
    }

    this.loading = true;

    const request: CreateReturnRequest = {
      orderId: this.orderId,
      returnType: this.form.value.returnType as any,
      reason: this.form.value.reason,
      description: this.form.value.description,
      items: this.items.map((item) => ({
        orderItemId: item.orderItemId,
        productId: item.productId,
        quantity: 1,
        reason: this.form.value.reason,
      })),
    };

    console.log('📤 Sending return request:', request); // Debug

    this.returnService.createReturn(request).subscribe({
      next: (res) => {
        this.loading = false;
        this.notify.success('Gửi yêu cầu đổi trả thành công!');
        this.modalRef.close(true);
      },
      error: (err) => {
        this.loading = false;
        console.error('❌ Create return error:', err);
        this.notify.error(err.error?.message || 'Không thể gửi yêu cầu');
      },
    });
  }

  cancel(): void {
    this.modalRef.close(false);
  }
}

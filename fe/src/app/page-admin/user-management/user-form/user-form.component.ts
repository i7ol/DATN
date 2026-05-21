import { Component, Input, OnInit } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { AccountControllerService } from 'src/app/api/admin/api/accountController.service';
import {
  UserCreationRequest,
  UserResponse,
  UserUpdateRequest,
} from 'src/app/api/admin/model/models';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html',
})
export class UserFormComponent implements OnInit {
  @Input() user?: UserResponse; // Nếu có = Edit, không có = Create
  @Input() isEdit = false;

  form: UserCreationRequest | UserUpdateRequest = {
    username: '',
    password: '',
    email: '',
    phone: '',
    address: '',
  };

  loading = false;
  isSubmitting = false;

  constructor(
    private modal: NzModalRef,
    private accountService: AccountControllerService,
    private message: NzMessageService,
  ) {}

  ngOnInit(): void {
    if (this.user && this.isEdit) {
      this.form = {
        email: this.user.email,
        phone: this.user.phone,
        address: this.user.address,
      };
    }
  }

  onSave(): void {
    this.isSubmitting = true;

    if (this.isEdit && this.user?.id) {
      // Update
      this.accountService
        .updateUser(this.user.id, this.form as UserUpdateRequest)
        .subscribe({
          next: () => {
            this.message.success('Cập nhật người dùng thành công');
            this.modal.close(true);
          },
          error: (err) => {
            console.error(err);
            this.message.error('Cập nhật thất bại');
          },
          complete: () => (this.isSubmitting = false),
        });
    } else {
      // Create
      this.accountService
        .createUser(this.form as UserCreationRequest)
        .subscribe({
          next: () => {
            this.message.success('Tạo người dùng thành công');
            this.modal.close(true);
          },
          error: (err) => {
            console.error(err);
            this.message.error(err.error?.message || 'Tạo người dùng thất bại');
          },
          complete: () => (this.isSubmitting = false),
        });
    }
  }

  onCancel(): void {
    this.modal.destroy();
  }
}

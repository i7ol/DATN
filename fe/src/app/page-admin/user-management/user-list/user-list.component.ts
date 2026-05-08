import { Component, OnInit } from '@angular/core';
import { AccountControllerService } from 'src/app/api/admin';
import { RoleControllerService } from 'src/app/api/admin';
import { UserResponse } from 'src/app/api/admin/model/userResponse';
import { NzModalService } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';
import { AssignRoleModalComponent } from '../../modals/assign-role-modal/assign-role-modal.component';
import { UserFormComponent } from '../user-form/user-form.component';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
})
export class UserListComponent implements OnInit {
  users: UserResponse[] = [];
  loading = false;
  page = 0;
  size = 10;
  total = 0;

  constructor(
    private accountService: AccountControllerService,
    private modal: NzModalService,
    private message: NzMessageService,
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }
  createNewUser(): void {
    this.modal.create({
      nzTitle: 'Thêm người dùng mới',
      nzContent: UserFormComponent,
      nzWidth: 700,
      nzOnOk: () => this.loadUsers(),
    });
  }

  editUser(user: UserResponse): void {
    this.modal.create({
      nzTitle: 'Chỉnh sửa người dùng',
      nzContent: UserFormComponent,
      nzComponentParams: {
        user: user,
        isEdit: true,
      },
      nzWidth: 700,
      nzOnOk: () => this.loadUsers(),
    });
  }
  loadUsers(): void {
    this.loading = true;
    this.accountService
      .getAllUsers({ page: this.page, size: this.size } as any)
      .subscribe({
        next: (res) => {
          this.users = res.content || [];
          this.total = res.totalElements || 0;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.message.error('Không thể tải danh sách người dùng');
        },
      });
  }

  openAssignRole(user: UserResponse): void {
    this.modal.create({
      nzTitle: 'Phân quyền người dùng',
      nzContent: AssignRoleModalComponent,
      nzComponentParams: { user: { ...user } },
      nzWidth: 600,
      nzOnOk: () => this.loadUsers(),
    });
  }

  deleteUser(id: number): void {
    this.modal.confirm({
      nzTitle: 'Xác nhận xóa',
      nzContent: 'Bạn có chắc muốn xóa người dùng này?',
      nzOkText: 'Xóa',
      nzOkDanger: true,
      nzOnOk: () => {
        this.accountService.deleteUser(id).subscribe({
          next: () => {
            this.message.success('Xóa người dùng thành công');
            this.loadUsers();
          },
          error: () => this.message.error('Xóa thất bại'),
        });
      },
    });
  }

  onPageChange(page: number): void {
    this.page = page - 1;
    this.loadUsers();
  }
}

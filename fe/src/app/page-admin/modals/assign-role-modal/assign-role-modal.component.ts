import { Component, Input, OnInit } from '@angular/core';
import { NzModalRef } from 'ng-zorro-antd/modal';
import { NzMessageService } from 'ng-zorro-antd/message';

import { AccountControllerService } from 'src/app/api/admin/api/accountController.service';
import { RoleControllerService } from 'src/app/api/admin/api/roleController.service';

import { UserResponse } from 'src/app/api/admin/model/userResponse';
import { RoleEntity } from 'src/app/api/admin/model/roleEntity';

@Component({
  selector: 'app-assign-role-modal',
  templateUrl: './assign-role-modal.component.html',
})
export class AssignRoleModalComponent implements OnInit {
  @Input() user!: UserResponse;

  allRoles: RoleEntity[] = [];
  selectedRoles: string[] = [];

  constructor(
    private modal: NzModalRef,
    private accountService: AccountControllerService,
    private roleService: RoleControllerService,
    private message: NzMessageService,
  ) {}

  ngOnInit(): void {
    this.loadRoles();

    this.selectedRoles = (this.user.roles || []).map((r: any) =>
      typeof r === 'string' ? r : r.name,
    );
  }

  loadRoles(): void {
    this.roleService.getAllRoles().subscribe({
      next: (res) => {
        console.log(res);
        this.allRoles = res.result || [];
      },
      error: () => this.message.error('Không thể tải danh sách quyền'),
    });
  }

  onSave(): void {
    if (!this.user.id) return;

    this.accountService
      .assignRoles(this.user.id, this.selectedRoles as any)
      .subscribe({
        next: () => {
          this.message.success('Phân quyền thành công');
          this.modal.close(true);
        },
        error: (err) => {
          console.error(err);
          this.message.error('Phân quyền thất bại');
        },
      });
  }

  onCancel(): void {
    this.modal.destroy();
  }
}

import { Component, OnInit } from '@angular/core';
import { RoleControllerService } from 'src/app/api/admin/api/roleController.service';
import { RoleEntity } from 'src/app/api/admin/model/roleEntity';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzModalService } from 'ng-zorro-antd/modal';

@Component({
  selector: 'app-role-management',
  templateUrl: './role-management.component.html',
})
export class RoleManagementComponent implements OnInit {
  roles: RoleEntity[] = [];
  loading = false;

  constructor(
    private roleService: RoleControllerService,
    private message: NzMessageService,
    private modal: NzModalService,
  ) {}

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(): void {
    this.loading = true;
    this.roleService.getAllRoles().subscribe({
      next: (res) => {
        this.roles = res.result || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.message.error('Không thể tải danh sách vai trò');
      },
    });
  }

  createRole(): void {
    // Có thể mở modal tạo role nếu cần
    this.message.info('Chức năng tạo role sẽ được triển khai sau');
  }

  viewPermissions(role: RoleEntity): void {
    this.roleService.getRolePermissions(role.name).subscribe({
      next: (res) => {
        const perms = Array.isArray(res.result)
          ? res.result
          : Array.from(res.result || []);
        this.modal.info({
          nzTitle: `Quyền của vai trò: ${role.name}`,
          nzContent: `<ul>${perms.map((p) => `<li>${p.name}</li>`).join('')}</ul>`,
          nzOkText: 'Đóng',
        });
      },
    });
  }
}

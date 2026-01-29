import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AccUserControllerService } from 'src/app/api/user/api/accUserController.service';
import { UserResponse } from 'src/app/api/user/model/userResponse';
import { NotificationService } from 'src/app/shared/services/notification.service';
@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit {
  loading = false;
  user!: UserResponse;
  form!: FormGroup;

  constructor(
    private accUserApi: AccUserControllerService,
    private fb: FormBuilder,
    private notify: NotificationService,
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  private loadProfile(): void {
    this.loading = true;

    this.accUserApi.me('response').subscribe({
      next: (resp: any) => {
        const handleBody = (body: any) => {
          const user = body?.result;

          if (!user) {
            this.notify.error('Không có dữ liệu người dùng');
            this.loading = false;
            return;
          }

          this.user = user;
          this.buildForm(user);
          this.loading = false;
        };

        if (resp.body instanceof Blob) {
          resp.body.text().then((text: string) => {
            handleBody(JSON.parse(text));
          });
        } else {
          handleBody(resp.body);
        }
      },
      error: (err) => {
        console.error(err);
        this.notify.error('Không tải được thông tin người dùng');
        this.loading = false;
      },
    });
  }

  private buildForm(user: UserResponse): void {
    this.form = this.fb.group({
      username: [{ value: user.username, disabled: true }],
      email: [user.email],
      phone: [user.phone],
      address: [user.address],
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { AccUserControllerService } from 'src/app/api/user/api/accUserController.service';
import { UserResponse } from 'src/app/api/user/model/userResponse';
import { NotificationService } from 'src/app/shared/services/notification.service';
import { LocationControllerService } from 'src/app/api/user/api/locationController.service';
@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit {
  loading = false;
  user!: UserResponse;
  form!: FormGroup;

  provinces: any[] = [];
  districts: any[] = [];
  wards: any[] = [];
  formTouched = false;
  constructor(
    private accUserApi: AccUserControllerService,
    private fb: FormBuilder,
    private notify: NotificationService,
    private locationApi: LocationControllerService,
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  private loadProfile(): void {
    this.loading = true;

    this.accUserApi.me('response').subscribe({
      next: (resp: any) => {
        const handleBody = (body: any) => {
          const user = body?.result ?? body;

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

  getValidationClass(fieldName: string): string {
    const control = this.form?.get(fieldName);
    if (!control || !this.formTouched) return '';
    return control.invalid && (control.dirty || control.touched)
      ? 'border-red-500'
      : 'border-green-500';
  }

  loadProvinces(): void {
    this.locationApi.provinces().subscribe({
      next: async (res: any) => {
        this.provinces =
          res instanceof Blob ? JSON.parse(await res.text()) : res;
      },
      error: () => this.notify.error('Không tải được tỉnh/thành'),
    });
  }

  loadDistricts(provinceCode: number): void {
    this.districts = [];
    this.wards = [];
    this.form.patchValue({ district: '', ward: '' });
    this.form.get('district')!.disable();
    this.form.get('ward')!.disable();
    if (!provinceCode) return;

    this.locationApi.districts(provinceCode).subscribe({
      next: async (res: any) => {
        const data = res instanceof Blob ? JSON.parse(await res.text()) : res;
        this.districts = data?.districts || [];
        this.form.get('district')!.enable();
      },
      error: () => this.notify.error('Không tải được quận/huyện'),
    });
  }

  loadWards(districtCode: number): void {
    this.wards = [];
    this.form.patchValue({ ward: '' });
    this.form.get('ward')!.disable();
    if (!districtCode) return;

    this.locationApi.wards(districtCode).subscribe({
      next: async (res: any) => {
        const data = res instanceof Blob ? JSON.parse(await res.text()) : res;
        this.wards = data?.wards || [];
        this.form.get('ward')!.enable();
      },
      error: () => this.notify.error('Không tải được phường/xã'),
    });
  }
  private buildForm(user: UserResponse): void {
    this.form = this.fb.group({
      username: [{ value: user.username, disabled: true }],
      email: [user.email],
      phone: [user.phone],
      province: [user.provinceCode || ''],
      district: [{ value: '', disabled: true }],
      ward: [{ value: '', disabled: true }],
      address: [user.address],
    });

    // Load tỉnh trước
    this.locationApi.provinces().subscribe({
      next: async (res: any) => {
        const data = res instanceof Blob ? JSON.parse(await res.text()) : res;

        this.provinces = data || [];

        if (user.provinceCode) {
          this.form.patchValue({ province: user.provinceCode });

          this.locationApi.districts(user.provinceCode).subscribe({
            next: async (dRes: any) => {
              const dData =
                dRes instanceof Blob ? JSON.parse(await dRes.text()) : dRes;

              this.districts = dData?.districts || [];
              this.form.get('district')!.enable();

              if (user.districtCode) {
                this.form.patchValue({ district: user.districtCode });

                this.locationApi.wards(user.districtCode).subscribe({
                  next: async (wRes: any) => {
                    const wData =
                      wRes instanceof Blob
                        ? JSON.parse(await wRes.text())
                        : wRes;

                    this.wards = wData?.wards || [];
                    this.form.get('ward')!.enable();

                    if (user.wardCode) {
                      this.form.patchValue({ ward: user.wardCode });
                    }
                  },
                });
              }
            },
          });
        }
      },
    });
  }
  saveProfile(): void {
    this.formTouched = true;

    if (this.form.invalid) {
      this.notify.error('Vui lòng kiểm tra lại thông tin');
      return;
    }

    this.loading = true;

    const raw = this.form.getRawValue();

    const payload = {
      email: raw.email,
      phone: raw.phone,
      address: raw.address,
      provinceCode: raw.province,
      districtCode: raw.district,
      wardCode: raw.ward,
    };

    this.accUserApi.updateMe(payload).subscribe({
      next: (res: any) => {
        const updatedUser = res?.result ?? res;

        // cập nhật lại user trong component
        this.user = updatedUser;

        // build lại form bằng dữ liệu mới
        this.buildForm(updatedUser);

        this.notify.success('Cập nhật thông tin thành công');
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.notify.error('Cập nhật thất bại');
        this.loading = false;
      },
    });
  }
}

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-auth-modal',
  templateUrl: './auth-modal.component.html',
  styleUrls: ['./auth-modal.component.scss'],
})
export class AuthModalComponent implements OnInit, OnDestroy {
  isLoginMode = true;
  hidePassword = true;

  loginForm!: FormGroup;
  registerForm!: FormGroup;

  loginLoading = false;
  registerLoading = false;
  loginError = '';
  registerError = '';

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private dialogRef: MatDialogRef<AuthModalComponent>,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.isLoginMode = data?.mode !== 'register';
  }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
      rememberMe: [false],
    });

    this.registerForm = this.fb.group(
      {
        username: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        password: ['', Validators.required],
        confirmPassword: ['', Validators.required],
        phone: [''],
        addressDetail: [''],
      },
      { validators: this.passwordMatchValidator }
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  passwordMatchValidator(form: FormGroup) {
    const p = form.get('password')?.value;
    const c = form.get('confirmPassword')?.value;
    return p && c && p !== c ? { mismatch: true } : null;
  }

  onLoginSubmit(): void {
    if (this.loginForm.invalid) return;

    this.loginLoading = true;
    this.loginError = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.loginLoading = false;
        this.dialogRef.close(true);
      },
      error: (err) => {
        this.loginError = err?.message || 'Đăng nhập thất bại';
        this.loginLoading = false;
      },
    });
  }

  onRegisterSubmit(): void {
    if (this.registerForm.invalid) return;

    const v = this.registerForm.value;

    this.registerLoading = true;
    this.registerError = '';

    this.authService
      .register({
        username: v.username,
        email: v.email,
        password: v.password,
        confirmPassword: v.confirmPassword,
        phone: v.phone,
        address: v.addressDetail,
      })
      .subscribe({
        next: () => {
          this.registerLoading = false;
          this.dialogRef.close(true);
        },
        error: () => {
          this.registerError = 'Đăng ký thất bại';
          this.registerLoading = false;
        },
      });
  }

  onTabChange(e: any) {
    // Logic này sẽ kích hoạt class .register-mode trong CSS giúp card rộng ra
    this.isLoginMode = e.index === 0;

    // Reset lỗi khi chuyển tab để form trông gọn gàng
    this.loginError = '';
    this.registerError = '';
  }

  closeDialog() {
    this.dialogRef.close();
  }
}

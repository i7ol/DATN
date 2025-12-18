// app/shared/components/auth-modal/auth-modal.component.ts
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-auth-modal',
  templateUrl: './auth-modal.component.html',
  styleUrls: ['./auth-modal.component.scss'],
})
export class AuthModalComponent implements OnInit, OnDestroy {
  isLoginMode = true;

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
    private router: Router,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.isLoginMode = data?.mode === 'login';
  }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required],
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
        this.dialogRef.close();
        this.router.navigate(['/']);
      },
      error: () => {
        this.loginError = 'Đăng nhập thất bại';
        this.loginLoading = false;
      },
    });
  }

  onRegisterSubmit(): void {
    if (this.registerForm.invalid) return;

    const v = this.registerForm.value;
    const payload = {
      username: v.username,
      email: v.email,
      password: v.password,
      confirmPassword: v.confirmPassword,
      phone: v.phone,
      address: v.addressDetail,
    };

    this.registerLoading = true;
    this.registerError = '';

    this.authService.register(payload).subscribe({
      next: () => {
        this.dialogRef.close();
        this.router.navigate(['/']);
      },
      error: () => {
        this.registerError = 'Đăng ký thất bại';
        this.registerLoading = false;
      },
    });
  }

  onTabChange(e: any) {
    this.isLoginMode = e.index === 0;
  }

  closeDialog() {
    this.dialogRef.close();
  }
}

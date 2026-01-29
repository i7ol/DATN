import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { environment } from 'src/environments/environment';
import {
  AuthResponse,
  User,
  LoginRequest,
  RegisterRequest,
} from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = environment.apiUrls.shopAuth;

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    this.restoreUser();
  }

  /* ================= RESTORE ================= */
  private restoreUser(): void {
    const token = this.getAccessToken();
    const userStr = localStorage.getItem('user');

    if (token && !this.isTokenExpired(token)) {
      if (userStr) {
        this.currentUserSubject.next(JSON.parse(userStr));
      } else {
        this.loadUser(); // gọi /auth/me
      }
    } else {
      this.clearStorage();
    }
  }

  /* ================= LOGIN ================= */
  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
      tap((res) => {
        localStorage.setItem('access_token', res.accessToken);
        localStorage.setItem('refresh_token', res.refreshToken);
      }),
      tap(() => this.loadUser()),
      catchError(() => throwError(() => new Error('Đăng nhập thất bại')))
    );
  }

  register(data: RegisterRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, data);
  }

  private loadUser(): void {
    this.http.get<User>(`${this.baseUrl}/me`).subscribe({
      next: (user) => {
        this.currentUserSubject.next(user);
        localStorage.setItem('user', JSON.stringify(user));
      },
      error: () => this.logout(),
    });
  }

  /* ================= REFRESH ================= */
  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) return throwError(() => new Error('No refresh token'));

    return this.http
      .post<AuthResponse>(`${this.baseUrl}/refresh-token`, { refreshToken })
      .pipe(
        tap((res) => {
          localStorage.setItem('access_token', res.accessToken);
          if (res.refreshToken) {
            localStorage.setItem('refresh_token', res.refreshToken);
          }
        }),
        catchError(() => {
          this.logout();
          return throwError(() => new Error('Refresh token failed'));
        })
      );
  }

  /* ================= LOGOUT ================= */
  logout(): void {
    this.clearStorage();
    this.currentUserSubject.next(null);
    this.router.navigate(['/']);
  }

  /* ================= TOKEN ================= */
  getAccessToken(): string | null {
    return localStorage.getItem('access_token');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refresh_token');
  }

  isAuthenticated(): boolean {
    const token = this.getAccessToken();
    return token ? !this.isTokenExpired(token) : false;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const decoded: any = jwtDecode(token);
      return decoded.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  /* ================= ROLE / PERMISSION ================= */
  isAdmin(): boolean {
    return this.currentUserSubject.value?.roles.includes('ADMIN') ?? false;
  }

  hasPermission(p: string): boolean {
    return this.currentUserSubject.value?.permissions.includes(p) ?? false;
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /* ================= UTILS ================= */
  private clearStorage(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user');
  }
}

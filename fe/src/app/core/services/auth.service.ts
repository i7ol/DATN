import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { environment } from 'src/environments/environment';

export interface User {
  id: number;
  username: string;
  email: string;
  fullName?: string;
  phone?: string;
  address?: string;
  roles: string[];
  permissions: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  authenticated: boolean;
  username: string;
  roles: string[];
  permissions: string[];
}

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

    if (token && userStr) {
      try {
        this.currentUserSubject.next(JSON.parse(userStr));
      } catch {
        this.clearStorage();
      }
    }
  }

  /* ================= LOGIN ================= */
  login(data: {
    username: string;
    password: string;
  }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data).pipe(
      tap((res) => {
        localStorage.setItem('access_token', res.accessToken);
        localStorage.setItem('refresh_token', res.refreshToken);
      }),
      tap(() => this.loadUserFromServer()),
      catchError((err) => throwError(() => new Error('Đăng nhập thất bại')))
    );
  }

  register(data: {
    username: string;
    email: string;
    password: string;
    phone?: string;
    address?: string;
  }): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, data);
  }

  private loadUserFromServer(): void {
    this.http.get<User>(`${this.baseUrl}/me`).subscribe({
      next: (user) => {
        this.currentUserSubject.next(user);
        localStorage.setItem('user', JSON.stringify(user));
      },
      error: () => this.logout(),
    });
  }

  /* ================= REFRESH ================= */
  refreshAccessToken(): Observable<AuthResponse> {
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

          const user = this.currentUserSubject.value;
          if (user) {
            this.currentUserSubject.next({
              ...user,
              roles: res.roles || [],
              permissions: res.permissions || [],
            });
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
    if (!token) return false;

    try {
      const decoded: any = jwtDecode(token);
      return decoded.exp && decoded.exp > Date.now() / 1000;
    } catch {
      return false;
    }
  }

  /* ================= UTILS ================= */
  private clearStorage(): void {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user');
  }

  isAdmin(): boolean {
    return this.currentUserSubject.value?.roles?.includes('ADMIN') ?? false;
  }

  hasPermission(p: string): boolean {
    return this.currentUserSubject.value?.permissions?.includes(p) ?? false;
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
}

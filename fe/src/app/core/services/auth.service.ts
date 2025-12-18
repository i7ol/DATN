import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

export interface User {
  id?: number;
  username: string;
  email?: string;
  phone?: string;
  address?: string;
  roles: string[];
  permissions: string[];
  isAuthenticated: boolean;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  phone?: string;
  address?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  authenticated: boolean;
  username: string;
  roles: string[];
  permissions: string[];
}

export interface UserInfoResponse {
  id?: number;
  username: string;
  email?: string;
  phone?: string;
  address?: string;
  roles: string[];
  permissions: string[];
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private authUrl = environment.apiUrl;

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();


  private _accessToken: string | null = null;
  private _refreshToken: string | null = null;

  constructor(private http: HttpClient) {
    this.loadTokens();

  }

  private loadTokens(): void {
    this._accessToken = localStorage.getItem('accessToken');
    this._refreshToken = localStorage.getItem('refreshToken');
    if (this._accessToken) {
      const user: User = {
        username: '',
        roles: [],
        permissions: [],
        isAuthenticated: true,
      };
      this.currentUserSubject.next(user);
    }
  }

  private saveTokens(accessToken: string, refreshToken: string): void {
    this._accessToken = accessToken;
    this._refreshToken = refreshToken;
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  }

  private clearTokens(): void {
    this._accessToken = null;
    this._refreshToken = null;
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('currentUser');
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.authUrl}/auth/login`, credentials)
      .pipe(
        tap((response) => {
          this.saveTokens(response.accessToken, response.refreshToken);
          const user: User = {
            username: response.username,
            roles: response.roles,
            permissions: response.permissions,
            isAuthenticated: true,
          };
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }),
        catchError((error) => {
          console.error('Login error:', error);
          return of({
            accessToken: '',
            refreshToken: '',
            authenticated: false,
            username: '',
            roles: [],
            permissions: [],
          });
        })
      );
  }

  register(userData: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.authUrl}/auth/register`, userData)
      .pipe(
        tap((response) => {
          if (response.authenticated) {
            this.saveTokens(response.accessToken, response.refreshToken);
            const user: User = {
              username: response.username,
              roles: response.roles,
              permissions: response.permissions,
              isAuthenticated: true,
            };
            localStorage.setItem('currentUser', JSON.stringify(user));
            this.currentUserSubject.next(user);
          }
        }),
        catchError((error) => {
          console.error('Registration error:', error);
          return of({
            accessToken: '',
            refreshToken: '',
            authenticated: false,
            username: '',
            roles: [],
            permissions: [],
          });
        })
      );
  }

  checkUsernameAvailability(username: string): Observable<boolean> {
    const takenUsernames = ['admin', 'user', 'test', 'demo'];
    const isAvailable = !takenUsernames.includes(username.toLowerCase());

    return of(isAvailable);
  }

  checkEmailAvailability(email: string): Observable<boolean> {
    const takenEmails = ['admin@example.com', 'user@example.com'];
    const isAvailable = !takenEmails.includes(email.toLowerCase());

    return of(isAvailable);
  }


  logout(): void {
    this.clearTokens();
    this.currentUserSubject.next(null);
  }

  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return (
      user?.roles.includes('ADMIN') ||
      user?.roles.includes('ROLE_ADMIN') ||
      false
    );
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }


  isAuthenticated(): boolean {
    return !!this._accessToken;
  }

 
  getAccessToken(): string | null {
    return this._accessToken;
  }

 
  doRefreshToken(): Observable<AuthResponse> {
    if (!this._refreshToken) {
      return of({
        accessToken: '',
        refreshToken: '',
        authenticated: false,
        username: '',
        roles: [],
        permissions: [],
      });
    }

    return this.http
      .post<AuthResponse>(`${this.authUrl}/auth/refresh-token`, {
        refreshToken: this._refreshToken,
      })
      .pipe(
        tap((response) => {
          if (response.authenticated) {
            this.saveTokens(response.accessToken, response.refreshToken);
          }
        }),
        catchError((error) => {
          console.error('Refresh token error:', error);
          this.logout();
          return of({
            accessToken: '',
            refreshToken: '',
            authenticated: false,
            username: '',
            roles: [],
            permissions: [],
          });
        })
      );
  }

  loadUserProfile(): Observable<UserInfoResponse> {
    if (!this._accessToken) {
      return of({
        username: '',
        roles: [],
        permissions: [],
      });
    }

    return this.http.get<UserInfoResponse>(`${this.authUrl}/user/profile`).pipe(
      tap((user) => {
        const currentUser: User = {
          id: user.id,
          username: user.username,
          email: user.email,
          phone: user.phone,
          address: user.address,
          roles: user.roles,
          permissions: user.permissions,
          isAuthenticated: true,
        };
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        this.currentUserSubject.next(currentUser);
      }),
      catchError((error) => {
        console.error('Load profile error:', error);
        return of({
          username: '',
          roles: [],
          permissions: [],
        });
      })
    );
  }
}

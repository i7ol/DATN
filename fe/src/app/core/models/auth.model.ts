export interface LoginRequest {
  username: string;
  password: string;
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

export interface RefreshTokenRequest {
  refreshToken: string;
}

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

export interface User {
  id: number;
  username: string;
  email: string;
  phone?: string;
  address?: string;
  roles: string[];
  permissions: string[];
}

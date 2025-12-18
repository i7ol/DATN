export interface RegisterRequest {
  username: string;
  password: string;
  confirmPassword: string;
  email: string;
  phone?: string;
  address?: string;
}

export interface RegisterResponse {
  success: boolean;
  message: string;
  username?: string;
  email?: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  roles: string[];
  permissions: string[];
  isAuthenticated: boolean;
}

export interface User {
  id: number;
  username: string;
  email: string;
  phone?: string;
  address?: string;
  roles: string[];
  permissions: string[];
}

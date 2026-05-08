import { Injectable } from '@angular/core';
import { CanActivateChild, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CanActivate } from '@angular/router';
@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivateChild, CanActivate {
  constructor(
    private auth: AuthService,
    private router: Router,
  ) {}

  canActivate(): boolean {
    return this.check();
  }

  canActivateChild(): boolean {
    return this.check();
  }

  private check(): boolean {
    if (this.auth.isAuthenticated() && this.auth.isAdmin()) {
      return true;
    }

    this.router.navigate(['/products']);
    return false;
  }
}

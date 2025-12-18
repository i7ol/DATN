// app/shared/header/header.component.ts
import { Component, HostListener, OnInit, OnDestroy } from '@angular/core';
import { CartService } from 'src/app/page-user/cart/cart.service';
import { AuthService, User } from 'src/app/core/services/auth.service';
import { DialogService } from 'src/app/core/services/dialog.service';
import { Observable } from 'rxjs';
import { CartItemResponse } from 'src/app/api/user';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  isScrolled = false;
  isCartHover = false;
  isAccountDropdownOpen = false;

  private hideCartTimeout: any = null;
  private hideAccountTimeout: any = null;

  cartCount$!: Observable<number>;
  cartItems$!: Observable<CartItemResponse[]>;
  totalPrice$!: Observable<number>;

  currentUser: User | null = null;
  isAdmin = false;
  currentRoute: string = '';
  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private dialogService: DialogService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cartCount$ = this.cartService.totalQuantity$;
    this.cartItems$ = this.cartService.items$;
    this.totalPrice$ = this.cartService.totalPrice$;

    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      this.isAdmin = this.authService.isAdmin();
    });
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.currentRoute = event.url;
      });
  }

  ngOnDestroy(): void {
    if (this.hideCartTimeout) clearTimeout(this.hideCartTimeout);
    if (this.hideAccountTimeout) clearTimeout(this.hideAccountTimeout);
  }

  @HostListener('window:scroll')
  onScroll() {
    this.isScrolled = window.scrollY > 10;
  }

  // CART METHODS
  onCartMouseEnter(): void {
    if (this.hideCartTimeout) {
      clearTimeout(this.hideCartTimeout);
      this.hideCartTimeout = null;
    }
    this.isCartHover = true;
  }

  onCartMouseLeave(): void {
    this.hideCartTimeout = setTimeout(() => {
      this.isCartHover = false;
    }, 300);
  }

  onDropdownMouseEnter(): void {
    if (this.hideCartTimeout) {
      clearTimeout(this.hideCartTimeout);
      this.hideCartTimeout = null;
    }
    this.isCartHover = true;
  }

  onDropdownMouseLeave(): void {
    this.hideCartTimeout = setTimeout(() => {
      this.isCartHover = false;
    }, 150);
  }

  // ACCOUNT METHODS
  onAccountMouseEnter(): void {
    if (this.hideAccountTimeout) {
      clearTimeout(this.hideAccountTimeout);
      this.hideAccountTimeout = null;
    }
    this.isAccountDropdownOpen = true;
  }

  onAccountMouseLeave(): void {
    this.hideAccountTimeout = setTimeout(() => {
      this.isAccountDropdownOpen = false;
    }, 300);
  }

  onAccountDropdownMouseEnter(): void {
    if (this.hideAccountTimeout) {
      clearTimeout(this.hideAccountTimeout);
      this.hideAccountTimeout = null;
    }
    this.isAccountDropdownOpen = true;
  }

  onAccountDropdownMouseLeave(): void {
    this.hideAccountTimeout = setTimeout(() => {
      this.isAccountDropdownOpen = false;
    }, 150);
  }

  // AUTH METHODS - Sử dụng DialogService
  openLoginModal(): void {
    this.dialogService.openAuthModal('login');
    this.isAccountDropdownOpen = false;
  }

  openRegisterModal(): void {
    this.dialogService.openAuthModal('register');
    this.isAccountDropdownOpen = false;
  }

  logout(): void {
    this.authService.logout();
    this.isAccountDropdownOpen = false;
  }

  getDisplayName(): string {
    if (this.currentUser) {
      return this.currentUser.username;
    }
    return 'Tài khoản';
  }

  closeAllDropdowns(): void {
    this.isCartHover = false;
    this.isAccountDropdownOpen = false;
  }

  isAdminRoute(route: string): boolean {
    const adminRoutes = [
      '/admin/products-admin',
      '/admin/inventory',
      '/admin/orders',
      '/admin/users',
      '/admin/payments',
    ];

    return adminRoutes.some((r) => this.currentRoute.startsWith(r));
  }
}

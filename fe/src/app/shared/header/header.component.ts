import { CartItemView } from './../../page-user/cart/cart.service';
import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { AuthModalComponent } from '../components/auth-modal/auth-modal.component';
import { CartService } from 'src/app/page-user/cart/cart.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { User } from 'src/app/core/models/user.model';
import { ProductUserControllerService } from 'src/app/api/user';
import { ProductResponse, PageProductResponse } from 'src/app/api/user';
import { Pageable } from 'src/app/api/user';
@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  isScrolled = false;
  isCartHover = false;
  isAccountDropdownOpen = false;

  cartItems: CartItemView[] = [];
  cartLoading = false;
  cartTotalPrice = 0;
  cartTotalQuantity = 0;

  currentUser: User | null = null;
  isAdmin = false;
  currentRoute = '';

  private hideCartTimeout: any;
  private hideAccountTimeout: any;
  private sub = new Subscription();
  cartCount$!: Observable<number>;

  isSearchOpen = false;
  searchQuery = '';
  searchResults: ProductResponse[] = [];
  searchLoading = false;
  private searchTimeout: any;

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private dialog: MatDialog,
    private productService: ProductUserControllerService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.cartCount$ = this.cartService.totalQuantity$;

    this.sub.add(
      this.cartService.items$.subscribe((i) => (this.cartItems = i || [])),
    );
    this.sub.add(
      this.cartService.totalPrice$.subscribe((p) => (this.cartTotalPrice = p)),
    );
    this.sub.add(
      this.cartService.totalQuantity$.subscribe(
        (q) => (this.cartTotalQuantity = q),
      ),
    );
    this.sub.add(
      this.cartService.loading$.subscribe((l) => (this.cartLoading = l)),
    );

    this.sub.add(
      this.authService.currentUser$.subscribe((user) => {
        this.currentUser = user;
        this.isAdmin = this.authService.isAdmin();
      }),
    );

    this.sub.add(
      this.router.events
        .pipe(filter((e) => e instanceof NavigationEnd))
        .subscribe((e: any) => {
          this.currentRoute = e.url;
          this.closeAllDropdowns();
        }),
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    clearTimeout(this.hideCartTimeout);
    clearTimeout(this.hideAccountTimeout);
  }

  @HostListener('window:scroll')
  onScroll() {
    this.isScrolled = window.scrollY > 10;
  }

  // ==================== SEARCH METHODS ====================

  onSearchMouseEnter() {
    clearTimeout(this.searchTimeout);
    this.isSearchOpen = true;
  }

  onSearchMouseLeave() {
    this.searchTimeout = setTimeout(() => {
      this.isSearchOpen = false;
    }, 500);
  }

  onSearchInputFocus() {
    this.isSearchOpen = true;
  }

  performSearch() {
    if (!this.searchQuery?.trim()) {
      this.searchResults = [];
      return;
    }

    this.searchLoading = true;

    this.productService
      .search(
        { page: 0, size: 8 } as Pageable,
        this.searchQuery.trim(),
        undefined,
      )
      .subscribe({
        next: (response: PageProductResponse) => {
          this.searchResults = response.content || [];
          this.searchLoading = false;
        },
        error: (err) => {
          console.error('Search error:', err);
          this.searchResults = [];
          this.searchLoading = false;
        },
      });
  }

  onSearchSubmit() {
    if (this.searchQuery?.trim()) {
      this.router.navigate(['/products'], {
        queryParams: { keyword: this.searchQuery.trim() },
      });
      this.closeSearch();
    }
  }

  // ←←← SỬA QUAN TRỌNG Ở ĐÂY ←←←
  selectProduct(product: ProductResponse, event?: Event) {
    if (event) {
      event.stopImmediatePropagation(); // Ngăn chặn mọi event khác
      event.preventDefault();
    }

    this.router
      .navigate(['/product', product.id])
      .then((success) => {
        if (success) {
          this.closeSearch();
        }
      })
      .catch((err) => console.error('Navigation error:', err));
  }

  closeSearch() {
    this.isSearchOpen = false;
    this.searchQuery = '';
    this.searchResults = [];
  }

  closeAllDropdowns() {
    this.isCartHover = false;
    this.isAccountDropdownOpen = false;
    this.closeSearch();
  }
  // CART
  onCartMouseEnter() {
    clearTimeout(this.hideCartTimeout);
    this.isCartHover = true;
  }
  onCartMouseLeave() {
    this.hideCartTimeout = setTimeout(() => (this.isCartHover = false), 300);
  }
  onDropdownMouseEnter() {
    clearTimeout(this.hideCartTimeout);
    this.isCartHover = true;
  }
  onDropdownMouseLeave() {
    this.hideCartTimeout = setTimeout(() => (this.isCartHover = false), 150);
  }

  // ACCOUNT
  onAccountMouseEnter() {
    clearTimeout(this.hideAccountTimeout);
    this.isAccountDropdownOpen = true;
  }
  onAccountMouseLeave() {
    this.hideAccountTimeout = setTimeout(
      () => (this.isAccountDropdownOpen = false),
      300,
    );
  }
  onAccountDropdownMouseEnter() {
    clearTimeout(this.hideAccountTimeout);
    this.isAccountDropdownOpen = true;
  }
  onAccountDropdownMouseLeave() {
    this.hideAccountTimeout = setTimeout(
      () => (this.isAccountDropdownOpen = false),
      150,
    );
  }

  openLoginModal() {
    this.dialog.open(AuthModalComponent, {
      width: '420px',
      data: { mode: 'login' },
    });
  }
  openRegisterModal() {
    this.dialog.open(AuthModalComponent, {
      width: '420px',
      data: { mode: 'register' },
    });
  }
  logout() {
    this.authService.logout();
  }

  getDisplayName(): string {
    return this.currentUser?.username || 'Tài khoản';
  }

  isAdminRoute(route: string): boolean {
    return route.startsWith('/admin');
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { finalize, tap } from 'rxjs/operators';
import { v4 as uuidv4 } from 'uuid';
import { CartItemResponse } from 'src/app/api/user/model/cartItemResponse';
import { CartResponse } from 'src/app/api/user/model/cartResponse';
import { environment } from 'src/environments/environment';
import { AuthService } from 'src/app/core/services/auth.service';

type CartItemView = CartItemResponse & {
  imageUrl: string;
};
@Injectable({
  providedIn: 'root',
})
export class CartService {
  private itemsSubject = new BehaviorSubject<CartItemView[]>([]);
  public items$ = this.itemsSubject.asObservable();

  private totalPriceSubject = new BehaviorSubject<number>(0);
  public totalPrice$ = this.totalPriceSubject.asObservable();

  private totalQuantitySubject = new BehaviorSubject<number>(0);
  public totalQuantity$ = this.totalQuantitySubject.asObservable();

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  private userId: number | null = null;
  private guestId: string | null = null;

  // FIX: Ensure correct base URL
  private baseUrl = `${environment.apiUrls.shopApp}/user/cart`;

  constructor(private http: HttpClient, private authService: AuthService) {
    this.initializeGuestId();

    this.authService.currentUser$.subscribe((user) => {
      if (user) {
        this.setUser(user.id);
      } else {
        this.refreshCart().subscribe();
      }
    });
  }

  private initializeGuestId(): void {
    // Try to get existing guest ID from localStorage
    const existingGuestId = localStorage.getItem('guestId');
    if (existingGuestId) {
      this.guestId = existingGuestId;
    } else {
      // Generate new guest ID
      this.guestId = uuidv4();

      localStorage.setItem('guestId', this.guestId);
    }
  }
  private buildParams(
    extra: Record<string, string | number> = {}
  ): Record<string, string | number> {
    const params: Record<string, string | number> = { ...extra };

    // Ưu tiên userId
    if (this.userId) {
      params.userId = this.userId;
    } else {
      // Chưa login → bắt buộc có guestId
      if (!this.guestId) {
        this.initializeGuestId();
      }
      params.guestId = this.guestId;
    }

    return params;
  }

  /* ===== REFRESH CART ===== */
  refreshCart(): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.http
      .get<CartResponse>(this.baseUrl, {
        params: this.buildParams(),
      })
      .pipe(
        tap((cart) => this.setCart(cart)),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  /* ===== ADD ITEM ===== */
  addItem(variantId: number, quantity: number): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.http
      .post<CartResponse>(`${this.baseUrl}/add`, null, {
        params: this.buildParams({ variantId, quantity }),
      })
      .pipe(
        tap((cart) => this.setCart(cart)),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  /* ===== UPDATE ITEM ===== */
  updateItem(variantId: number, quantity: number): Observable<CartResponse> {
    if (quantity < 1) {
      return this.removeItem(variantId);
    }

    this.loadingSubject.next(true);

    return this.http
      .put<CartResponse>(`${this.baseUrl}/update`, null, {
        params: this.buildParams({ variantId, quantity }),
      })
      .pipe(
        tap((cart) => this.setCart(cart)),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  removeItem(variantId: number): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.http
      .delete<CartResponse>(`${this.baseUrl}/remove`, {
        params: this.buildParams({ variantId }),
      })
      .pipe(
        tap((cart) => this.setCart(cart)),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  /* ===== CLEAR CART ===== */
  clearCart(): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.http
      .delete<CartResponse>(`${this.baseUrl}/clear`, {
        params: this.buildParams(),
      })
      .pipe(
        tap((cart) => this.setCart(cart)),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  /* ===== MERGE GUEST → USER ===== */
  mergeGuestToUser(userId: number): Observable<CartResponse> {
    if (!this.guestId) {
      this.initializeGuestId();
    }

    this.loadingSubject.next(true);

    return this.http
      .post<CartResponse>(`${this.baseUrl}/merge`, null, {
        params: { guestId: this.guestId, userId },
      })
      .pipe(
        tap((cart) => {
          localStorage.removeItem('guestId');
          this.guestId = null;
          this.setCart(cart);
        }),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  /* ===== SET CART TO SUBJECTS ===== */
  private setCart(cart: CartResponse) {
    const items: CartItemView[] =
      cart.items?.map((item) => {
        const imageUrl =
          item.images && item.images.length > 0 ? item.images[0].url : null;

        return {
          ...item,
          imageUrl: imageUrl
            ? imageUrl.startsWith('http')
              ? imageUrl
              : `${environment.apiUrls.shopApp}${imageUrl}`
            : 'assets/no-image.png',
        };
      }) || [];

    this.itemsSubject.next(items);
    this.totalPriceSubject.next(cart.totalPrice || 0);

    const totalQty =
      cart.quantity ?? items.reduce((sum, i) => sum + (i.quantity || 0), 0);

    this.totalQuantitySubject.next(totalQty);
  }

  /* ===== SET USER/GUEST ===== */
  setUser(userId: number) {
    this.userId = userId;
    // If we have a guest cart, merge it
    if (this.guestId) {
      this.mergeGuestToUser(userId).subscribe();
    } else {
      this.refreshCart().subscribe();
    }
  }

  setGuest(guestId: string) {
    this.guestId = guestId;
    localStorage.setItem('guestId', guestId);
    this.refreshCart().subscribe();
  }

  // Helper method to get current cart identifiers
  getCartIdentifiers() {
    return {
      userId: this.userId,
      guestId: this.guestId,
    };
  }
}

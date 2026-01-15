import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap, finalize } from 'rxjs/operators';
import { v4 as uuidv4 } from 'uuid';
import { CartProxyControllerService } from 'src/app/api/user';
import { CartItemResponse } from 'src/app/api/user/model/cartItemResponse';
import { CartResponse } from 'src/app/api/user/model/cartResponse';
import { AuthService } from 'src/app/core/services/auth.service';
import { HttpResponse } from '@angular/common/http';
import { switchMap } from 'rxjs/operators';
export type CartItemView = CartItemResponse & { imageUrl: string };

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

  constructor(
    private cartApi: CartProxyControllerService,
    private authService: AuthService
  ) {
    this.initializeGuestId();

    this.refreshCart().subscribe({
      next: (cart) => console.log('Guest cart initialized:', cart),
      error: (err) => console.error('Error initializing guest cart:', err),
    });

    this.authService.currentUser$.subscribe((user) => {
      if (user) {
        this.setUser(user.id);
      }
    });
  }

  // ================== GUEST ID ==================
  private initializeGuestId(): void {
    const existingGuestId = localStorage.getItem('guestId');
    if (existingGuestId) {
      this.guestId = existingGuestId;
    } else {
      this.guestId = uuidv4();
      localStorage.setItem('guestId', this.guestId);
      console.log('Generated new guestId:', this.guestId);
    }
  }

  // ================== GETTERS ==================
  get cartItemsCount(): number {
    return this.itemsSubject.value.reduce(
      (sum, item) => sum + (item.quantity || 0),
      0
    );
  }

  private handleCartResponse(resp: any): void {
    if (!resp) return;

    // Trường hợp OpenAPI trả HttpResponse
    if (resp instanceof HttpResponse) {
      const body = resp.body;

      if (body instanceof Blob) {
        body.text().then((text: string) => {
          const cart = JSON.parse(text) as CartResponse;
          this.setCart(cart);
        });
      } else {
        this.setCart(body as CartResponse);
      }
      return;
    }

    // Trường hợp trả trực tiếp CartResponse
    this.setCart(resp as CartResponse);
  }

  // ================== CART OPERATIONS ==================
  refreshCart(): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.cartApi
      .getCart(this.userId ?? undefined, this.guestId ?? undefined, 'response')
      .pipe(
        tap((resp: any) => {
          if (resp.body instanceof Blob) {
            resp.body.text().then((text: string) => {
              const cart = JSON.parse(text) as CartResponse;
              this.setCart(cart);
            });
          } else {
            this.setCart(resp.body as CartResponse);
          }
        }),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  addItem(variantId: number, quantity: number): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.cartApi
      .addItem(
        variantId,
        quantity,
        this.userId ?? undefined,
        this.guestId ?? undefined
      )
      .pipe(
        switchMap(() => this.refreshCart()),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  updateItem(variantId: number, quantity: number): Observable<CartResponse> {
    if (quantity < 1) return this.removeItem(variantId);

    this.loadingSubject.next(true);

    return this.cartApi
      .updateItem(
        variantId,
        quantity,
        this.userId ?? undefined,
        this.guestId ?? undefined
      )
      .pipe(
        switchMap(() => this.refreshCart()),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  removeItem(variantId: number): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.cartApi
      .removeItem(
        variantId,
        this.userId ?? undefined,
        this.guestId ?? undefined
      )
      .pipe(
        switchMap(() => this.refreshCart()),
        finalize(() => this.loadingSubject.next(false))
      );
  }

  clearCart(): Observable<CartResponse> {
    this.loadingSubject.next(true);

    const clear$ = this.userId
      ? this.cartApi.clearUserCart(this.userId)
      : this.cartApi.clearGuestCart(this.guestId!);

    return clear$.pipe(
      tap(() => {
        this.itemsSubject.next([]);
        this.totalPriceSubject.next(0);
        this.totalQuantitySubject.next(0);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  mergeGuestToUser(userId: number): Observable<CartResponse> {
    if (!this.guestId) this.initializeGuestId();
    this.loadingSubject.next(true);
    return this.cartApi.mergeGuestCart(this.guestId!, userId).pipe(
      tap((cart) => {
        localStorage.removeItem('guestId');
        this.guestId = null;
        this.setCart(cart);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  // ================== HELPER METHODS ==================
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
              : `assets/${imageUrl}`
            : 'assets/no-image.png',
        };
      }) || [];

    this.itemsSubject.next(items);
    this.totalPriceSubject.next(cart.totalPrice || 0);
    const totalQty =
      cart.quantity ?? items.reduce((sum, i) => sum + (i.quantity || 0), 0);
    this.totalQuantitySubject.next(totalQty);
  }

  setUser(userId: number) {
    this.userId = userId;
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

  getCartIdentifiers() {
    return { userId: this.userId, guestId: this.guestId };
  }

  // ================== OPTIONAL FORM CHECK ==================
  isFormValid(formValues?: any): boolean {
    // Bạn có thể tuỳ chỉnh theo formGroup hoặc các field
    if (!formValues) return false;
    return Object.values(formValues).every((v) => v !== null && v !== '');
  }
}

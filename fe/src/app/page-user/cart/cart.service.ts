import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, Subscription, throwError } from 'rxjs';
import {
  tap,
  finalize,
  catchError,
  shareReplay,
  distinctUntilChanged,
} from 'rxjs/operators';
import { v4 as uuidv4 } from 'uuid';

import { CartProxyControllerService } from 'src/app/api/user';
import { CartItemResponse } from 'src/app/api/user/model/cartItemResponse';
import { CartResponse } from 'src/app/api/user/model/cartResponse';
import { AuthService } from 'src/app/core/services/auth.service';

export type CartItemView = CartItemResponse & { imageUrl?: string };

@Injectable({
  providedIn: 'root',
})
export class CartService implements OnDestroy {
  // ================= STATE =================
  private itemsSubject = new BehaviorSubject<CartItemView[]>([]);
  items$ = this.itemsSubject.asObservable().pipe(shareReplay(1));

  private totalPriceSubject = new BehaviorSubject<number>(0);
  totalPrice$ = this.totalPriceSubject.asObservable().pipe(shareReplay(1));

  private totalQuantitySubject = new BehaviorSubject<number>(0);
  totalQuantity$ = this.totalQuantitySubject
    .asObservable()
    .pipe(shareReplay(1));

  private loadingSubject = new BehaviorSubject<boolean>(false);
  loading$ = this.loadingSubject.asObservable();

  private guestId: string | null = null;
  private isLoggedIn = false;

  private sub = new Subscription();

  constructor(
    private cartApi: CartProxyControllerService,
    private authService: AuthService,
  ) {
    this.initGuestId();
    this.refreshCart().subscribe();

    this.sub.add(
      this.authService.currentUser$
        .pipe(distinctUntilChanged())
        .subscribe((user) => {
          const wasLoggedIn = this.isLoggedIn;
          this.isLoggedIn = !!user;

          if (!wasLoggedIn && user && this.guestId) {
            this.mergeGuestToUser();
          } else {
            this.refreshCart().subscribe();
          }
        }),
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  // ================= GUEST =================
  private initGuestId(): void {
    this.guestId = localStorage.getItem('guestId');
    if (!this.guestId) {
      this.guestId = uuidv4();
      localStorage.setItem('guestId', this.guestId);
    }
  }

  private get guestHeader(): string | undefined {
    return this.isLoggedIn ? undefined : (this.guestId ?? undefined);
  }

  // ================= OPTIMISTIC UPDATE =================
  addItem(variantId: number, quantity: number = 1): Observable<CartResponse> {
    this.loadingSubject.next(true);

    const currentItems = [...this.itemsSubject.getValue()];
    const existing = currentItems.find((i) => i.variantId === variantId);

    if (existing) {
      existing.quantity = (existing.quantity || 0) + quantity;
    } else {
      currentItems.push({
        variantId: variantId,
        quantity: quantity,
        productName: 'Đang tải...',
        price: 0,
        unitPrice: 0,
        color: '',
        size: '',
        images: [],
      } as CartItemView);
    }

    this.itemsSubject.next(currentItems);
    this.recalculateTotals();

    return this.cartApi.addItem(variantId, quantity, this.guestHeader).pipe(
      tap((cart: CartResponse) => this.setCart(cart)),
      catchError((err) => {
        console.error('Add item error', err);
        this.refreshCart().subscribe(); // rollback
        return throwError(err);
      }),
      finalize(() => this.loadingSubject.next(false)),
    );
  }

  updateItem(variantId: number, quantity: number): Observable<CartResponse> {
    this.loadingSubject.next(true);

    const currentItems = [...this.itemsSubject.getValue()];
    const item = currentItems.find((i) => i.variantId === variantId);
    if (item) item.quantity = quantity;

    this.itemsSubject.next(currentItems);
    this.recalculateTotals();

    return this.cartApi.updateItem(variantId, quantity, this.guestHeader).pipe(
      tap((cart) => this.setCart(cart)),
      catchError((err) => {
        this.refreshCart().subscribe();
        return throwError(err);
      }),
      finalize(() => this.loadingSubject.next(false)),
    );
  }

  removeItem(variantId: number): Observable<CartResponse> {
    this.loadingSubject.next(true);

    const currentItems = this.itemsSubject
      .getValue()
      .filter((i) => i.variantId !== variantId);
    this.itemsSubject.next(currentItems);
    this.recalculateTotals();

    return this.cartApi.removeItem(variantId, this.guestHeader).pipe(
      tap((cart) => this.setCart(cart)),
      catchError((err) => {
        this.refreshCart().subscribe();
        return throwError(err);
      }),
      finalize(() => this.loadingSubject.next(false)),
    );
  }

  clearCart(): Observable<CartResponse> {
    this.loadingSubject.next(true);
    this.itemsSubject.next([]);
    this.totalPriceSubject.next(0);
    this.totalQuantitySubject.next(0);

    return this.cartApi.clearCart(this.guestHeader).pipe(
      tap((cart) => this.setCart(cart)),
      finalize(() => this.loadingSubject.next(false)),
    );
  }

  refreshCart(): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.cartApi.getCart(this.guestHeader).pipe(
      tap((cart) => this.setCart(cart)),
      finalize(() => this.loadingSubject.next(false)),
    );
  }

  private mergeGuestToUser(): void {
    if (!this.guestId) return;
    this.loadingSubject.next(true);

    this.cartApi
      .mergeGuestCart(this.guestId)
      .pipe(
        tap((cart) => {
          localStorage.removeItem('guestId');
          this.guestId = null;
          this.setCart(cart);
        }),
        finalize(() => this.loadingSubject.next(false)),
      )
      .subscribe();
  }

  // ================= HELPER =================
  private setCart(cart: CartResponse): void {
    const items: CartItemView[] = (cart.items || []).map((item) => ({
      ...item,
      imageUrl: item.images?.[0]?.url ?? 'assets/img/no-image.png',
      size: item.size || '',
      color: item.color || '',
    }));

    this.itemsSubject.next(items);
    this.totalPriceSubject.next(cart.totalPrice ? Number(cart.totalPrice) : 0);
    this.totalQuantitySubject.next(cart.quantity || 0);
  }

  private recalculateTotals(): void {
    const items = this.itemsSubject.getValue();
    let qty = 0;
    let price = 0;

    items.forEach((i) => {
      qty += i.quantity || 0;
      price += (i.quantity || 0) * (Number(i.price) || 0);
    });

    this.totalQuantitySubject.next(qty);
    this.totalPriceSubject.next(price);
  }

  // ================= METHOD ĐƯỢC SỬ DỤNG Ở CHECKOUT =================
  getCartIdentifiers(): { guestId?: string; userId?: number } {
    const user = this.authService.getCurrentUser?.();
    if (user?.id) {
      return { userId: user.id };
    }
    return { guestId: this.guestId ?? undefined };
  }
}

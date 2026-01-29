import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import {
  tap,
  finalize,
  distinctUntilChanged,
  shareReplay,
} from 'rxjs/operators';
import { v4 as uuidv4 } from 'uuid';

import { CartProxyControllerService } from 'src/app/api/user';
import { CartItemResponse } from 'src/app/api/user/model/cartItemResponse';
import { CartResponse } from 'src/app/api/user/model/cartResponse';
import { AuthService } from 'src/app/core/services/auth.service';
import { of } from 'rxjs';
import { switchMap } from 'rxjs/operators';

export type CartItemView = CartItemResponse & { imageUrl: string };

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

    // Lắng nghe login/logout
    this.sub.add(
      this.authService.currentUser$
        .pipe(distinctUntilChanged())
        .subscribe((user) => {
          const wasLoggedIn = this.isLoggedIn;
          this.isLoggedIn = !!user;

          if (!wasLoggedIn && user && this.guestId) {
            // login lần đầu → merge cart
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

    console.log('[Cart] guestId:', this.guestId);
  }

  /**
   * Chỉ gửi X-Guest-Id khi CHƯA login
   */
  private get guestHeader(): string | undefined {
    return this.isLoggedIn ? undefined : (this.guestId ?? undefined);
  }

  refreshCart(): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.cartApi.getCart(this.guestHeader).pipe(
      switchMap((res: any) => {
        if (res instanceof Blob) {
          return this.blobToJson<CartResponse>(res);
        }
        return of(res);
      }),
      tap((cart) => {
        console.log('[Cart] refreshCart parsed', cart);
        this.setCart(cart);
      }),
      finalize(() => this.loadingSubject.next(false)),
    );
  }

  addItem(variantId: number, quantity: number): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.cartApi.addItem(variantId, quantity, this.guestHeader).pipe(
      switchMap((res: any) => {
        if (res instanceof Blob) {
          return this.blobToJson<CartResponse>(res);
        }
        return of(res);
      }),
      tap((cart) => {
        console.log('[Cart] addItem parsed', cart);
        this.setCart(cart);
      }),
      finalize(() => this.loadingSubject.next(false)),
    );
  }

  updateItem(variantId: number, quantity: number): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.cartApi.updateItem(variantId, quantity, this.guestHeader).pipe(
      tap((cart) => this.setCart(cart)),
      finalize(() => this.loadingSubject.next(false)),
    );
  }

  removeItem(variantId: number): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.cartApi.removeItem(variantId, this.guestHeader).pipe(
      tap((cart) => this.setCart(cart)),
      finalize(() => this.loadingSubject.next(false)),
    );
  }

  clearCart(): Observable<CartResponse> {
    this.loadingSubject.next(true);

    return this.cartApi.clearCart(this.guestHeader).pipe(
      tap((cart) => this.setCart(cart)),
      finalize(() => this.loadingSubject.next(false)),
    );
  }

  // ================= MERGE =================
  private mergeGuestToUser(): void {
    if (!this.guestId) return;

    this.loadingSubject.next(true);

    this.cartApi.mergeGuestCart(this.guestId, 'body').subscribe({
      next: (cart) => {
        console.log('[Cart] merged guest → user');
        localStorage.removeItem('guestId');
        this.guestId = null;
        this.setCart(cart);
      },
      error: (err) => console.error('[Cart] merge error', err),
      complete: () => this.loadingSubject.next(false),
    });
  }

  // ================= HELPER =================
  private setCart(cart: CartResponse): void {
    const items: CartItemView[] =
      cart.items?.map((item) => ({
        ...item,
        imageUrl: item.images?.[0]?.url ?? 'assets/no-image.png',
      })) ?? [];

    this.itemsSubject.next(items);
    this.totalPriceSubject.next(cart.totalPrice ?? 0);
    this.totalQuantitySubject.next(
      cart.quantity ?? items.reduce((s, i) => s + (i.quantity ?? 0), 0),
    );
  }
  getCartIdentifiers(): {
    guestId?: string;
    userId?: number;
  } {
    const user = this.authService.getCurrentUser?.();

    if (user?.id) {
      return {
        userId: user.id,
      };
    }

    return {
      guestId: this.guestId ?? undefined,
    };
  }

  private blobToJson<T>(blob: Blob): Observable<T> {
    return new Observable<T>((observer) => {
      const reader = new FileReader();

      reader.onload = () => {
        try {
          observer.next(JSON.parse(reader.result as string));
          observer.complete();
        } catch (e) {
          observer.error(e);
        }
      };

      reader.onerror = (e) => observer.error(e);
      reader.readAsText(blob);
    });
  }
}

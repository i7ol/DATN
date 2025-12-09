import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { CartResponse, CartItemResponse } from 'src/app/api/user';
import { CartUserControllerService } from 'src/app/api/user/api/cartUserController.service';

@Injectable({ providedIn: 'root' })
export class CartService {
  private cartSubject = new BehaviorSubject<CartResponse | null>(null);
  cart$ = this.cartSubject.asObservable();

  private guestId!: string;
  private userId?: number;

  constructor(private cartApi: CartUserControllerService) {
    this.initGuest();
    this.loadCart().subscribe();
  }

  /* ===== INIT GUEST ===== */
  private generateGuestId(): string {
    return 'guest-' + Math.random().toString(36).substring(2, 15);
  }

  private initGuest() {
    let gid = localStorage.getItem('guest_id');
    if (!gid) {
      gid = this.generateGuestId();
      localStorage.setItem('guest_id', gid);
    }
    this.guestId = gid;
  }

  /* ===== PARSE RESPONSE (FIX GỐC LỖI) ===== */
  private handleCartResponse(resp: any) {
    if (resp instanceof Blob) {
      return resp.text().then((text) => JSON.parse(text) as CartResponse);
    }
    return Promise.resolve(resp as CartResponse);
  }

  /* ===== API ===== */
  loadCart() {
    return this.cartApi.getCart(this.userId, this.guestId).pipe(
      tap(async (resp) => {
        const cart = await this.handleCartResponse(resp);
        this.cartSubject.next(cart);
      })
    );
  }

  addItem(productId: number, quantity = 1) {
    return this.cartApi
      .addItem(productId, quantity, this.userId, this.guestId)
      .pipe(
        tap(async (resp) => {
          const cart = await this.handleCartResponse(resp);
          this.cartSubject.next(cart);
        })
      );
  }

  updateItem(productId: number, quantity: number) {
    if (quantity <= 0) return this.removeItem(productId);

    return this.cartApi
      .updateItem(productId, quantity, this.userId, this.guestId)
      .pipe(
        tap(async (resp) => {
          const cart = await this.handleCartResponse(resp);
          this.cartSubject.next(cart);
        })
      );
  }

  removeItem(productId: number) {
    return this.cartApi.removeItem(productId, this.userId, this.guestId).pipe(
      tap(async (resp) => {
        const cart = await this.handleCartResponse(resp);
        this.cartSubject.next(cart);
      })
    );
  }

  /* ===== SELECTORS ===== */
  items$ = this.cart$.pipe(map((cart) => cart?.items ?? []));

  totalQuantity$ = this.cart$.pipe(
    map(
      (cart) => cart?.items?.reduce((sum, i) => sum + (i.quantity ?? 0), 0) ?? 0
    )
  );

  totalPrice$ = this.cart$.pipe(map((cart) => cart?.totalPrice ?? 0));
}

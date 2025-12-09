// cart.component.ts
import { Component } from '@angular/core';
import { CartService } from './cart.service';
import { Observable } from 'rxjs';
import { CartItemResponse } from 'src/app/api/user';

@Component({
  selector: 'cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss'],
})
export class CartComponent {
  items$: Observable<CartItemResponse[]>;
  totalPrice$: Observable<number>;

  constructor(private cartService: CartService) {
    this.items$ = this.cartService.items$;
    this.totalPrice$ = this.cartService.totalPrice$;
  }

  increase(item: CartItemResponse) {
    this.cartService
      .updateItem(item.productId!, (item.quantity ?? 0) + 1)
      .subscribe();
  }

  decrease(item: CartItemResponse) {
    this.cartService
      .updateItem(item.productId!, (item.quantity ?? 0) - 1)
      .subscribe();
  }

  remove(item: CartItemResponse) {
    this.cartService.removeItem(item.productId!).subscribe();
  }
}

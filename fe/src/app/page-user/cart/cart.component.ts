import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';

import { CartItemResponse } from 'src/app/api/user/model/cartItemResponse';
import { ConfirmDialogComponent } from 'src/app/shared/confirm-dialog/confirm-dialog.component';
import { CartService } from './cart.service';

@Component({
  selector: 'cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss'],
})
export class CartComponent implements OnInit, OnDestroy {
  items$ = this.cartService.items$;
  loading$ = this.cartService.loading$;
  totalQuantity$ = this.cartService.totalQuantity$;
  totalPrice$ = this.cartService.totalPrice$;

  private sub = new Subscription();

  constructor(
    private cartService: CartService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.cartService.refreshCart().subscribe();
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  increase(item: CartItemResponse) {
    if (!item.variantId) return;
    this.cartService
      .updateItem(item.variantId, (item.quantity ?? 0) + 1)
      .subscribe();
  }

  decrease(item: CartItemResponse) {
    if (!item.variantId || (item.quantity ?? 0) <= 1) return;
    this.cartService
      .updateItem(item.variantId, (item.quantity ?? 0) - 1)
      .subscribe();
  }

  remove(item: CartItemResponse) {
    if (!item.variantId) return;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '360px',
      data: {
        title: 'Xóa sản phẩm',
        message: 'Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?',
        confirmText: 'Xóa',
        cancelText: 'Hủy',
      },
    });

    this.sub.add(
      dialogRef.afterClosed().subscribe((confirmed) => {
        if (confirmed) {
          this.cartService.removeItem(item.variantId!).subscribe();
        }
      })
    );
  }

  clearCart() {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '380px',
      data: {
        title: 'Xóa toàn bộ giỏ hàng',
        message: 'Hành động này không thể hoàn tác. Bạn có chắc chắn?',
        confirmText: 'Xóa tất cả',
        cancelText: 'Hủy',
      },
    });

    this.sub.add(
      dialogRef.afterClosed().subscribe((confirmed) => {
        if (confirmed) {
          this.cartService.clearCart().subscribe();
        }
      })
    );
  }

  goToCheckout(): void {
    this.router.navigate(['/checkout']);
  }

  continueShopping(): void {
    this.router.navigate(['/']);
  }
}

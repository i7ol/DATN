import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { CartItemResponse } from 'src/app/api/user/model/cartItemResponse';
import { CartService } from './cart.service';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from 'src/app/shared/confirm-dialog/confirm-dialog.component';

@Component({
  selector: 'cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.scss'],
})
export class CartComponent implements OnInit, OnDestroy {
  items$!: Observable<CartItemResponse[]>;
  totalPrice$!: Observable<number>;
  totalQuantity$!: Observable<number>;
  loading$!: Observable<boolean>;

  private sub = new Subscription();

  constructor(
    private cartService: CartService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  /* ================= INIT ================= */
  ngOnInit(): void {
    this.items$ = this.cartService.items$;
    this.totalPrice$ = this.cartService.totalPrice$;
    this.totalQuantity$ = this.cartService.totalQuantity$;
    this.loading$ = this.cartService.loading$;
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  /* ================= CART ACTIONS ================= */

  refreshCart(): void {
    this.sub.add(this.cartService.refreshCart().subscribe());
  }

  increase(item: CartItemResponse): void {
    if (!item.variantId) return;

    this.sub.add(
      this.cartService
        .updateItem(item.variantId, (item.quantity ?? 0) + 1)
        .subscribe()
    );
  }

  decrease(item: CartItemResponse): void {
    if (!item.variantId || (item.quantity ?? 0) <= 1) return;

    this.sub.add(
      this.cartService
        .updateItem(item.variantId, (item.quantity ?? 0) - 1)
        .subscribe()
    );
  }

  remove(item: CartItemResponse): void {
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

  clearCart(): void {
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

  /* ================= NAVIGATION ================= */

  goToCheckout(): void {
    this.router.navigate(['/checkout']);
  }

  continueShopping(): void {
    this.router.navigate(['/']);
  }
}

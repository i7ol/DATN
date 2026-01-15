import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CartService } from '../cart/cart.service';
@Component({
  selector: 'app-payment-result',
  templateUrl: './payment-result.component.html',
  styleUrls: ['./payment-result.component.scss'],
})
export class PaymentResultComponent implements OnInit, OnDestroy {
  success = false;
  orderId = '';
  transactionId = '';
  message = '';
  isLoading = true;

  countdown = 5;
  private redirectTimer: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.success = params['success'] === 'true';
      this.orderId = params['orderId'] || '';
      this.transactionId = params['transactionId'] || '';
      this.message = params['message'] || this.getDefaultMessage();

      if (this.success && !sessionStorage.getItem('cartCleared')) {
        this.cartService.clearCart().subscribe(() => {
          sessionStorage.setItem('cartCleared', 'true');
        });
      }

      this.isLoading = false;
      this.startCountdown();
    });
  }

  ngOnDestroy(): void {
    if (this.redirectTimer) {
      clearInterval(this.redirectTimer);
    }
  }

  private getDefaultMessage(): string {
    return this.success
      ? 'Thanh toán thành công! Cảm ơn bạn đã mua sắm.'
      : 'Thanh toán thất bại. Vui lòng thử lại.';
  }

  private startCountdown(): void {
    this.redirectTimer = setInterval(() => {
      this.countdown--;
      if (this.countdown <= 0) {
        clearInterval(this.redirectTimer);
        this.goHome();
      }
    }, 1000);
  }

  goHome(): void {
    sessionStorage.removeItem('cartCleared');
    this.router.navigate(['/']);
  }

  viewOrder(): void {
    if (this.orderId) {
      this.router.navigate(['/orders', this.orderId]);
    }
  }

  retryPayment(): void {
    if (this.orderId) {
      this.router.navigate(['/checkout'], {
        queryParams: { orderId: this.orderId },
      });
    }
  }
}

import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentUserControllerService } from 'src/app/api/user/api/paymentUserController.service';

@Component({
  selector: 'app-payment-result',
  templateUrl: './payment-result.component.html',
  styleUrls: ['./payment-result.component.scss'],
})
export class PaymentResultComponent implements OnInit, OnDestroy {
  success = false;
  orderId: string = '';
  paymentId: string = '';
  transactionId: string = '';
  message: string = 'Đang xử lý kết quả thanh toán...';
  isLoading = true;
  redirectTimer: any = null;
  countdown = 5;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: PaymentUserControllerService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      console.log('Payment result params:', params);

      // Xử lý kết quả từ VNPay callback
      if (params['vnp_ResponseCode'] || params['vnp_TxnRef']) {
        this.handleVNPayCallback(params);
      }
      // Xử lý kết quả thông thường
      else {
        this.success = params['success'] === 'true';
        this.orderId = params['orderId'] || '';
        this.paymentId = params['paymentId'] || '';
        this.transactionId = params['transactionId'] || '';
        this.message = params['message'] || this.getMessage();
        this.isLoading = false;
        this.setupAutoRedirect();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.redirectTimer) {
      clearInterval(this.redirectTimer);
    }
  }

  private handleVNPayCallback(params: any): void {
    console.log('Processing VNPay callback:', params);

    const vnpResponseCode = params['vnp_ResponseCode'];
    const vnpTxnRef = params['vnp_TxnRef'];
    const vnpTransactionNo = params['vnp_TransactionNo'];

    // Xử lý kết quả thanh toán
    this.success = vnpResponseCode === '00';
    this.orderId = vnpTxnRef ? this.extractOrderId(vnpTxnRef) : '';
    this.transactionId = vnpTransactionNo || '';

    if (this.success) {
      this.message = 'Thanh toán VNPAY thành công!';

      // Cập nhật trạng thái payment nếu có orderId
      if (this.orderId) {
        this.updatePaymentStatus(this.orderId, vnpTransactionNo);
      } else {
        this.isLoading = false;
        this.setupAutoRedirect();
      }
    } else {
      this.message = `Thanh toán VNPAY thất bại (Mã lỗi: ${
        vnpResponseCode || 'unknown'
      })`;
      this.isLoading = false;
      this.setupAutoRedirect();
    }
  }

  private extractOrderId(vnpTxnRef: string): string {
    // VNP_TxnRef thường là "00000123" -> cần bỏ số 0 đầu
    try {
      // Nếu là chuỗi có số 0 ở đầu, bỏ đi
      if (vnpTxnRef.match(/^0+/)) {
        return parseInt(vnpTxnRef).toString();
      }
      return vnpTxnRef;
    } catch (e) {
      return vnpTxnRef;
    }
  }

  private updatePaymentStatus(orderId: string, transactionId: string): void {
    if (!orderId || !transactionId) {
      this.isLoading = false;
      this.setupAutoRedirect();
      return;
    }

    // Gọi API mark-paid để cập nhật trạng thái payment
    // Ghi chú: Cần import InternalPaymentControllerService để gọi markPaidByOrderId
    // Nếu không có internal service, có thể skip bước này
    // VNPay callback sẽ tự động xử lý ở backend

    this.isLoading = false;
    this.setupAutoRedirect();
  }

  private getMessage(): string {
    return this.success
      ? 'Thanh toán thành công! Cảm ơn bạn đã mua sắm.'
      : 'Đã xảy ra lỗi trong quá trình thanh toán.';
  }

  private setupAutoRedirect(): void {
    // Tự động chuyển về trang chủ sau 5 giây
    this.redirectTimer = setInterval(() => {
      this.countdown--;

      if (this.countdown <= 0) {
        clearInterval(this.redirectTimer);
        this.redirectHome();
      }
    }, 1000);
  }

  redirectHome(): void {
    if (this.redirectTimer) {
      clearInterval(this.redirectTimer);
    }
    this.router.navigate(['/']);
  }

  viewOrder(): void {
    if (this.orderId) {
      this.router.navigate(['/orders', this.orderId]);
    }
  }

  continueShopping(): void {
    this.router.navigate(['/products']);
  }

  retryPayment(): void {
    if (this.orderId) {
      this.router.navigate(['/checkout'], {
        queryParams: { orderId: this.orderId },
      });
    }
  }
}

import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { CartService } from '../cart/cart.service';
import { AuthService, User } from 'src/app/core/services/auth.service';

import {
  OrderUserControllerService,
  PaymentUserControllerService,
  ShippingUserControllerService,
  CartItemResponse,
  ShippingResponse,
  CheckoutRequest,
  OrderResponse,
} from 'src/app/api/user';

/* ===================== ADDRESS ===================== */

interface Province {
  code: string;
  name: string;
}

interface District {
  code: string;
  name: string;
  province_code: string;
}

interface Ward {
  code: string;
  name: string;
  district_code: string;
}

/* ===================== SHIPPING METHOD ===================== */

interface ShippingMethod {
  id: string;
  name: string;
  description: string;
  fee: number;
  estimatedDays: string;
}

/* ===================== CART ===================== */

interface CartItemWithImage extends CartItemResponse {
  imageUrl?: string;
}

interface OrderSummary {
  subtotal: number;
  shipping: number;
  discount: number;
  total: number;
}

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss'],
})
export class CheckoutComponent implements OnInit, OnDestroy {
  checkoutForm!: FormGroup;
  currentUser: User | null = null;

  isSubmitting = false;
  isProcessingPayment = false;

  /* Cart */
  cartItems: CartItemWithImage[] = [];
  cartTotal = 0;
  cartItemsCount = 0;
  private cartSubscription!: Subscription;

  /* Address */
  provinces: Province[] = [];
  districts: District[] = [];
  wards: Ward[] = [];

  /* Shipping Methods (Hardcode vì API không có) */
  shippingMethods: ShippingMethod[] = [
    {
      id: 'STANDARD',
      name: 'Giao hàng tiêu chuẩn',
      description: 'Giao hàng trong 3-5 ngày làm việc',
      fee: 30000,
      estimatedDays: '3-5 ngày',
    },
    {
      id: 'EXPRESS',
      name: 'Giao hàng nhanh',
      description: 'Giao hàng trong 1-2 ngày làm việc',
      fee: 50000,
      estimatedDays: '1-2 ngày',
    },
    {
      id: 'FREE',
      name: 'Giao hàng miễn phí',
      description: 'Giao hàng miễn phí cho đơn trên 500,000đ',
      fee: 0,
      estimatedDays: '5-7 ngày',
    },
  ];
  selectedShippingMethodId = 'STANDARD';
  selectedShippingMethodFee = 30000;

  /* Discount */
  discountCode = '';
  discountApplied = false;
  discountAmount = 0;
  discountPercentage = 0;

  /* Payment */
  selectedPaymentMethod = 'COD';

  /* Summary */
  orderSummary: OrderSummary = {
    subtotal: 0,
    shipping: 0,
    discount: 0,
    total: 0,
  };

  constructor(
    private fb: FormBuilder,
    private cartService: CartService,
    private authService: AuthService,
    private orderApi: OrderUserControllerService,
    private paymentApi: PaymentUserControllerService,
    private router: Router
  ) {}

  /* ===================== LIFECYCLE ===================== */

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.initForm();
    this.loadProvinces();
    this.loadCart();
    this.calculateOrderSummary();
  }

  ngOnDestroy(): void {
    if (this.cartSubscription) {
      this.cartSubscription.unsubscribe();
    }
  }

  /* ===================== FORM ===================== */

  private initForm(): void {
    const u = this.currentUser;

    this.checkoutForm = this.fb.group({
      email: [u?.email || '', [Validators.required, Validators.email]],
      name: [u?.username || '', Validators.required],
      phone: [
        u?.phone || '',
        [
          Validators.required,
          Validators.pattern(/^(0|\+84)[3|5|7|8|9][0-9]{8}$/),
        ],
      ],

      province: ['', Validators.required],
      district: ['', Validators.required],
      ward: ['', Validators.required],
      addressDetail: ['', [Validators.required, Validators.minLength(10)]],

      shippingMethod: ['STANDARD', Validators.required], // Đây là string
      paymentMethod: ['COD', Validators.required],
      note: [''],
      discountCode: [''],
    });
  }

  /* ===================== CART ===================== */

  private loadCart(): void {
    this.cartSubscription = this.cartService.items$.subscribe((items) => {
      this.cartItems = items.map((i) => ({
        ...i,
        imageUrl: i.images?.[0]?.url || '',
      }));

      this.cartItemsCount = items.reduce((s, i) => s + (i.quantity || 0), 0);

      this.cartTotal = items.reduce(
        (s, i) => s + (i.price || 0) * (i.quantity || 0),
        0
      );

      // Áp dụng free shipping nếu đạt điều kiện
      this.applyFreeShippingIfEligible();
      this.calculateOrderSummary();
    });
  }

  /* ===================== SHIPPING ===================== */

  onShippingMethodChange(methodId: string): void {
    this.selectedShippingMethodId = methodId;
    const method = this.shippingMethods.find((m) => m.id === methodId);
    if (method) {
      this.selectedShippingMethodFee = method.fee;
      this.checkoutForm.patchValue({
        shippingMethod: method.name, // Gửi tên phương thức
      });
    }
    this.calculateOrderSummary();
  }

  private applyFreeShippingIfEligible(): void {
    // Nếu tổng đơn hàng >= 500,000đ thì áp dụng free shipping
    if (this.cartTotal >= 500000) {
      const freeMethod = this.shippingMethods.find((m) => m.id === 'FREE');
      if (freeMethod) {
        this.selectedShippingMethodId = 'FREE';
        this.selectedShippingMethodFee = 0;
        this.checkoutForm.patchValue({ shippingMethod: freeMethod.name });
      }
    }
  }

  /* ===================== PAYMENT ===================== */

  onPaymentMethodChange(method: string): void {
    this.selectedPaymentMethod = method;
    this.checkoutForm.patchValue({ paymentMethod: method });
  }

  /* ===================== ADDRESS (MOCK) ===================== */

  private loadProvinces(): void {
    this.provinces = [
      { code: 'HN', name: 'Hà Nội' },
      { code: 'HCM', name: 'Hồ Chí Minh' },
      { code: 'DN', name: 'Đà Nẵng' },
    ];
  }

  onProvinceChange(): void {
    this.checkoutForm.patchValue({ district: '', ward: '' });
    this.districts = [
      { code: 'BD', name: 'Ba Đình', province_code: 'HN' },
      { code: 'CG', name: 'Cầu Giấy', province_code: 'HN' },
    ];
  }

  onDistrictChange(): void {
    this.checkoutForm.patchValue({ ward: '' });
    this.wards = [
      { code: 'P1', name: 'Phường 1', district_code: 'BD' },
      { code: 'P2', name: 'Phường 2', district_code: 'BD' },
    ];
  }

  /* ===================== DISCOUNT ===================== */

  applyDiscount(): void {
    if (!this.discountCode.trim()) return;

    // Logic áp dụng discount
    if (this.discountCode.toUpperCase() === 'FREESHIP') {
      this.discountAmount = this.selectedShippingMethodFee;
      this.discountApplied = true;
      alert('Đã áp dụng mã miễn phí vận chuyển');
    } else if (this.discountCode.toUpperCase() === 'SALE10') {
      this.discountPercentage = 10;
      this.discountAmount = this.cartTotal * 0.1;
      this.discountApplied = true;
      alert('Đã áp dụng giảm giá 10%');
    } else {
      alert('Mã giảm giá không hợp lệ');
      return;
    }

    this.calculateOrderSummary();
  }

  removeDiscount(): void {
    this.discountCode = '';
    this.discountApplied = false;
    this.discountAmount = 0;
    this.discountPercentage = 0;
    this.calculateOrderSummary();
  }

  /* ===================== SUMMARY ===================== */

  calculateOrderSummary(): void {
    const subtotal = this.cartTotal;
    const shipping = this.selectedShippingMethodFee;

    // Áp dụng free shipping discount nếu có mã
    if (
      this.discountCode.toUpperCase() === 'FREESHIP' &&
      this.discountApplied
    ) {
      this.discountAmount = shipping;
    }

    const discount = this.discountAmount;
    const total = subtotal + shipping - discount;

    this.orderSummary = { subtotal, shipping, discount, total };
  }

  /* ===================== SUBMIT ===================== */

  submitOrder(): void {
    if (this.checkoutForm.invalid || this.cartItemsCount === 0) {
      alert('Vui lòng kiểm tra thông tin');
      return;
    }

    this.isSubmitting = true;
    const v = this.checkoutForm.value;

    // Tạo CheckoutRequest
    const checkoutRequest: CheckoutRequest = {
      shippingAddress: `${v.addressDetail}, ${v.ward}, ${v.district}, ${v.province}`,
      shippingProvince: v.province,
      shippingDistrict: v.district,
      shippingWard: v.ward,
      shippingNote: v.note || undefined,
      shippingMethod: v.shippingMethod, // Tên phương thức vận chuyển
      paymentMethod: v.paymentMethod,

      // Nếu là khách (không đăng nhập) hoặc luôn gửi guestName
      guestName: v.name,
      guestEmail: !this.currentUser ? v.email : undefined,
      guestPhone: !this.currentUser ? v.phone : undefined,

      // Billing address (có thể giống shipping address)
      billingAddress: `${v.addressDetail}, ${v.ward}, ${v.district}, ${v.province}`,
      billingProvince: v.province,
      billingDistrict: v.district,
      billingWard: v.ward,
    };

    console.log('Checkout request:', checkoutRequest);

    // Gửi request
    this.orderApi.checkout(checkoutRequest).subscribe({
      next: (order) => {
        this.isSubmitting = false;

        // Xóa giỏ hàng sau khi đặt hàng thành công
        this.cartService.clearCart().subscribe();

        // Xử lý thanh toán nếu chọn QR
        if (this.selectedPaymentMethod === 'QR') {
          this.processQRPayment(order.id!);
        } else {
          // Điều hướng đến trang chi tiết đơn hàng
          this.router.navigate(['/orders', order.id]);
        }
      },
      error: (err) => {
        this.isSubmitting = false;
        console.error('Checkout error:', err);

        let errorMessage = 'Tạo đơn hàng thất bại. Vui lòng thử lại.';

        if (err.error?.message) {
          errorMessage = err.error.message;
        } else if (err.status === 400) {
          errorMessage = 'Thông tin không hợp lệ. Vui lòng kiểm tra lại.';
        } else if (err.status === 401) {
          errorMessage = 'Vui lòng đăng nhập để đặt hàng.';
        } else if (err.status === 500) {
          errorMessage = 'Lỗi hệ thống. Vui lòng thử lại sau.';
        }

        alert(errorMessage);
      },
    });
  }

  private processQRPayment(orderId: number): void {
    this.isProcessingPayment = true;

    // Tạo payment request
    const paymentRequest = {
      orderId: orderId,
      method: 'VNPAY_QR',
    };

    this.paymentApi.createPayment(paymentRequest).subscribe({
      next: (paymentResponse) => {
        this.isProcessingPayment = false;

        // Kiểm tra payment response
        const paymentAny = paymentResponse as any;

        if (paymentAny?.qrCode) {
          // Hiển thị mã QR
          this.showQRCode(paymentAny.qrCode);
        } else if (paymentAny?.paymentUrl) {
          // Chuyển hướng đến trang thanh toán
          window.location.href = paymentAny.paymentUrl;
        } else if (paymentResponse?.transactionId) {
          // Nếu chỉ có transactionId, chuyển đến order detail
          this.router.navigate(['/orders', orderId]);
        } else {
          // Mặc định chuyển đến order detail
          this.router.navigate(['/orders', orderId]);
        }
      },
      error: (err) => {
        this.isProcessingPayment = false;
        console.error('QR Payment error:', err);
        alert(
          'Không thể tạo mã QR thanh toán. Vui lòng thử lại hoặc chọn COD.'
        );
        // Vẫn chuyển đến order detail nếu lỗi
        this.router.navigate(['/orders', orderId]);
      },
    });
  }

  private showQRCode(qrCodeData: string): void {
    // Tạo modal hiển thị QR code
    const modal = `
      <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg p-6 max-w-sm">
          <h3 class="text-lg font-semibold mb-4">Quét mã QR để thanh toán</h3>
          <div class="flex justify-center mb-4">
            <img src="${qrCodeData}" alt="QR Code" class="w-64 h-64">
          </div>
          <p class="text-sm text-gray-600 mb-4 text-center">
            Quét mã QR bằng ứng dụng ngân hàng để thanh toán
          </p>
          <div class="flex justify-center">
            <button 
              onclick="this.closest('.fixed').remove()" 
              class="px-4 py-2 bg-black text-white rounded-lg hover:bg-gray-800"
            >
              Đã thanh toán
            </button>
          </div>
        </div>
      </div>
    `;

    // Thêm modal vào DOM
    const div = document.createElement('div');
    div.innerHTML = modal;
    document.body.appendChild(div.firstChild as Node);
  }

  /* ===================== UTILITIES ===================== */

  formatCurrency(v: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(v);
  }

  getShippingMethodName(id: string): string {
    const method = this.shippingMethods.find((m) => m.id === id);
    return method?.name || 'Phương thức vận chuyển';
  }
}

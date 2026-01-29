import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { GuestPaymentRequest } from 'src/app/api/user/model/guestPaymentRequest';
import { UserPaymentRequest } from 'src/app/api/user/model/userPaymentRequest';
import { CartService } from 'src/app/page-user/cart/cart.service';
import { CartItemResponse } from 'src/app/api/user/model/cartItemResponse';
import { NotificationService } from 'src/app/shared/services/notification.service';
import { LocationControllerService } from 'src/app/api/user/api/locationController.service';
import { OrderProxyControllerService } from 'src/app/api/user/api/orderProxyController.service';
import { CheckoutRequest } from 'src/app/api/user/model/checkoutRequest';
import { CheckoutItemRequest } from 'src/app/api/user/model/checkoutItemRequest';
import { PaymentResponse } from 'src/app/api/user/model/paymentResponse';
import { PaymentProxyControllerService } from 'src/app/api/user/api/paymentProxyController.service';
@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss'],
})
export class CheckoutComponent implements OnInit, OnDestroy {
  checkoutForm!: FormGroup;
  private sub = new Subscription();
  discountCode = '';
  discountApplied = false;
  discountPercentage = 0;
  isProcessingPayment = false;

  isLoading = false;
  isSubmitting = false;
  cartItems: CartItemResponse[] = [];
  shippingMethods = [
    { id: 'STANDARD', name: 'Giao hàng tiêu chuẩn', fee: 0 },
    { id: 'EXPRESS', name: 'Giao hàng nhanh', fee: 30000 },
  ];

  selectedShippingMethodId: 'STANDARD' | 'EXPRESS' = 'STANDARD';

  formTouched = false;
  selectedPaymentMethod: 'COD' | 'VNPAY' = 'VNPAY';

  // ===== Add these two =====
  isFormValid(): boolean {
    return this.checkoutForm ? this.checkoutForm.valid : false;
  }

  get cartItemsCount(): number {
    return this.cartItems.reduce((sum, i) => sum + (i.quantity || 0), 0);
  }

  provinces: any[] = [];
  districts: any[] = [];
  wards: any[] = [];

  orderSummary = { subtotal: 0, shipping: 0, discount: 0, total: 0 };

  constructor(
    private fb: FormBuilder,
    private cartService: CartService,
    private orderApi: OrderProxyControllerService,
    private locationApi: LocationControllerService,
    private notify: NotificationService,
    private paymentApi: PaymentProxyControllerService,
  ) {}

  ngOnInit(): void {
    this.buildForm();
    this.bindCart();
    this.loadProvinces();
    this.listenLocationChanges();
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  getValidationClass(fieldName: string): string {
    const control = this.checkoutForm?.get(fieldName);
    if (!control || !this.formTouched) return '';
    return control.invalid && (control.dirty || control.touched)
      ? 'is-invalid'
      : 'is-valid';
  }

  onPaymentMethodChange(paymentMethod: 'COD' | 'VNPAY'): void {
    this.selectedPaymentMethod = paymentMethod;
  }

  private buildForm(): void {
    this.checkoutForm = this.fb.group({
      email: ['abc@gmail.com', [Validators.required, Validators.email]],
      name: ['Lò A', Validators.required],
      phone: ['0398287981', Validators.required],
      province: ['', Validators.required],
      district: [{ value: '', disabled: true }, Validators.required],
      ward: [{ value: '', disabled: true }, Validators.required],
      addressDetail: ['Nhà bên trái, phía bên phải', Validators.required],
      note: [''],
    });
  }
  applyDiscount(): void {
    this.discountApplied = true;
    this.discountPercentage = 0;
  }

  removeDiscount(): void {
    this.discountApplied = false;
    this.discountPercentage = 0;
  }

  onShippingMethodChange(methodId: 'STANDARD' | 'EXPRESS'): void {
    this.selectedShippingMethodId = methodId;

    const method = this.shippingMethods.find((m) => m.id === methodId);
    const shippingFee = method?.fee ?? 0;

    const subtotal = this.orderSummary.subtotal;
    this.orderSummary = {
      ...this.orderSummary,
      total: subtotal + shippingFee,
    };
  }

  private listenLocationChanges(): void {
    this.sub.add(
      this.checkoutForm
        .get('province')!
        .valueChanges.subscribe((code) => this.loadDistricts(code)),
    );
    this.sub.add(
      this.checkoutForm
        .get('district')!
        .valueChanges.subscribe((code) => this.loadWards(code)),
    );
  }

  private bindCart(): void {
    this.isLoading = true;
    this.sub.add(
      this.cartService.items$.subscribe((items) => {
        this.cartItems = items || [];
        this.calculateSummary();
        this.isLoading = false;
      }),
    );

    // refresh cart from API
    this.cartService.refreshCart().subscribe();
  }

  loadProvinces(): void {
    this.locationApi.provinces().subscribe({
      next: async (res: any) => {
        this.provinces =
          res instanceof Blob ? JSON.parse(await res.text()) : res;
      },
      error: () => this.notify.error('Không tải được tỉnh/thành'),
    });
  }

  loadDistricts(provinceCode: number): void {
    this.districts = [];
    this.wards = [];
    this.checkoutForm.patchValue({ district: '', ward: '' });
    this.checkoutForm.get('district')!.disable();
    this.checkoutForm.get('ward')!.disable();
    if (!provinceCode) return;

    this.locationApi.districts(provinceCode).subscribe({
      next: async (res: any) => {
        const data = res instanceof Blob ? JSON.parse(await res.text()) : res;
        this.districts = data?.districts || [];
        this.checkoutForm.get('district')!.enable();
      },
      error: () => this.notify.error('Không tải được quận/huyện'),
    });
  }

  loadWards(districtCode: number): void {
    this.wards = [];
    this.checkoutForm.patchValue({ ward: '' });
    this.checkoutForm.get('ward')!.disable();
    if (!districtCode) return;

    this.locationApi.wards(districtCode).subscribe({
      next: async (res: any) => {
        const data = res instanceof Blob ? JSON.parse(await res.text()) : res;
        this.wards = data?.wards || [];
        this.checkoutForm.get('ward')!.enable();
      },
      error: () => this.notify.error('Không tải được phường/xã'),
    });
  }

  private calculateSummary(): void {
    const subtotal = this.cartItems.reduce(
      (s, i) => s + (i.price || 0) * (i.quantity || 0),
      0,
    );

    const method = this.shippingMethods.find(
      (m) => m.id === this.selectedShippingMethodId,
    );
    const shippingFee = method?.fee ?? 0;

    this.orderSummary = {
      subtotal,
      shipping: shippingFee,
      discount: 0,
      total: subtotal + shippingFee,
    };
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(amount || 0);
  }

  submitOrder(): void {
    this.formTouched = true;
    if (this.checkoutForm.invalid || this.cartItems.length === 0) {
      this.notify.error('Vui lòng nhập đầy đủ thông tin');
      return;
    }

    if (this.isSubmitting) return;
    this.isSubmitting = true;

    const ids = this.cartService.getCartIdentifiers();
    const isGuest = !ids.userId;

    const checkoutItems: CheckoutItemRequest[] = this.cartItems.map((i) => ({
      productId: i.productId!,
      variantId: i.variantId!,
      quantity: i.quantity!,
    }));

    const payload: CheckoutRequest = {
      ...(isGuest ? { guestId: ids.guestId } : {}),

      guestName: isGuest ? this.checkoutForm.value.name : undefined,
      guestEmail: isGuest ? this.checkoutForm.value.email : undefined,
      guestPhone: isGuest ? this.checkoutForm.value.phone : undefined,

      shippingAddress: this.checkoutForm.value.addressDetail,
      shippingProvince: this.checkoutForm.value.province,
      shippingDistrict: this.checkoutForm.value.district,
      shippingWard: this.checkoutForm.value.ward,
      shippingNote: this.checkoutForm.value.note,

      paymentMethod: this.selectedPaymentMethod,
      shippingMethod: this.selectedShippingMethodId,
      items: checkoutItems,
    };

    this.orderApi.checkout(payload).subscribe({
      next: async (res: any) => {
        const order = res instanceof Blob ? JSON.parse(await res.text()) : res;

        console.log('ORDER ID', order.id);
        if (this.selectedPaymentMethod === 'VNPAY') {
          this.createPayment(order.id);
          return;
        }

        this.completeOrder();
      },
      error: () => {
        this.notify.error('Checkout thất bại');
        this.isSubmitting = false;
      },
    });
  }

  private async handlePaymentResponse(res: any): Promise<void> {
    let data: any;

    if (res instanceof Blob) {
      const text = await res.text();
      data = JSON.parse(text);
    } else {
      data = res;
    }

    console.log('PAYMENT RESPONSE:', data);

    if (!data?.paymentUrl) {
      this.notify.error('Không nhận được link thanh toán');
      this.isSubmitting = false;
      return;
    }

    window.location.href = data.paymentUrl;
  }

  private handlePaymentError(): void {
    this.notify.error('Tạo thanh toán thất bại');
    this.isSubmitting = false;
  }

  private createPayment(orderId: number): void {
    const ids = this.cartService.getCartIdentifiers();
    const isGuest = !ids.userId;

    if (isGuest) {
      const payload: GuestPaymentRequest = {
        orderId,
        guestId: ids.guestId!,
        method: 'VNPAY',
      };

      this.paymentApi.guestPayment(payload).subscribe({
        next: (res) => this.handlePaymentResponse(res),
        error: () => this.handlePaymentError(),
      });
    } else {
      const payload: UserPaymentRequest = {
        orderId,
        method: 'VNPAY',
      };

      this.paymentApi.userPayment(payload).subscribe({
        next: (res) => this.handlePaymentResponse(res),
        error: () => this.handlePaymentError(),
      });
    }
  }

  private completeOrder(): void {
    this.notify.success('Đặt hàng thành công!');
    this.cartService.clearCart().subscribe();
    this.isSubmitting = false;
  }
}

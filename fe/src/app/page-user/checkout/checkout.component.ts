import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { CartService } from 'src/app/page-user/cart/cart.service';
import { CartItemResponse } from 'src/app/api/user/model/cartItemResponse';
import { NotificationService } from 'src/app/shared/services/notification.service';
import { LocationControllerService } from 'src/app/api/user/api/locationController.service';
import { OrderProxyControllerService } from 'src/app/api/user/api/orderProxyController.service';
import { CheckoutRequest } from 'src/app/api/user/model/checkoutRequest';
import { CheckoutItemRequest } from 'src/app/api/user/model/checkoutItemRequest';
import { PaymentProxyControllerService } from 'src/app/api/user/api/paymentProxyController.service';
import { AccUserControllerService } from 'src/app/api/user/api/accUserController.service';
import { CheckoutRequestPaymentMethodEnum } from 'src/app/api/user/model/checkoutRequest';

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
  paymentEnum = CheckoutRequestPaymentMethodEnum;
  formTouched = false;
  selectedPaymentMethod: CheckoutRequestPaymentMethodEnum | null = null;

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
    private accUserApi: AccUserControllerService,
  ) {}

  ngOnInit(): void {
    this.buildForm();
    this.bindCart();
    this.listenLocationChanges();
    this.loadProvinces();
    this.loadUserInfo();
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  // ==================== FORM & VALIDATION ====================
  isFormValid(): boolean {
    return this.checkoutForm ? this.checkoutForm.valid : false;
  }

  get cartItemsCount(): number {
    return this.cartItems.reduce((sum, i) => sum + (i.quantity || 0), 0);
  }

  private buildForm(): void {
    this.checkoutForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', Validators.required],
      phone: ['', Validators.required],
      province: ['', Validators.required],
      district: [{ value: '', disabled: true }, Validators.required],
      ward: [{ value: '', disabled: true }, Validators.required],
      addressDetail: ['', Validators.required],
      note: [''],
    });
  }

  getValidationClass(fieldName: string): string {
    const control = this.checkoutForm?.get(fieldName);
    if (!control || !this.formTouched) return '';
    return control.invalid && (control.dirty || control.touched)
      ? 'is-invalid'
      : 'is-valid';
  }

  // ==================== AUTOFILL ĐỊA CHỈ ====================
  private loadUserInfo(): void {
    this.isLoading = true;

    this.accUserApi.me().subscribe({
      next: (res: any) => {
        const user = res?.result || res || {};

        if (user.id) {
          this.checkoutForm.patchValue(
            {
              email: user.email || '',
              name: user.username || '',
              phone: user.phone || '',
              addressDetail: user.address || '',
            },
            { emitEvent: false },
          );

          if (user.provinceCode) {
            this.autoFillAddress(
              user.provinceCode,
              user.districtCode,
              user.wardCode,
            );
          }
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      },
    });
  }

  private async autoFillAddress(
    provinceCode: any,
    districtCode?: any,
    wardCode?: any,
  ): Promise<void> {
    // Set tỉnh
    this.checkoutForm
      .get('province')
      ?.setValue(provinceCode, { emitEvent: true });

    // Chờ load quận
    await this.waitForData(() => this.districts.length > 0);

    // Enable và set quận
    this.enableDistrictField();
    if (districtCode) {
      this.checkoutForm
        .get('district')
        ?.setValue(districtCode, { emitEvent: true });
    }

    // Chờ load phường
    await this.waitForData(() => this.wards.length > 0);

    // Enable và set phường
    this.enableWardField();
    if (wardCode) {
      this.checkoutForm.get('ward')?.setValue(wardCode);
    }
  }

  private enableDistrictField(): void {
    this.checkoutForm.get('district')?.enable({ emitEvent: false });
  }

  private enableWardField(): void {
    this.checkoutForm.get('ward')?.enable({ emitEvent: false });
  }

  private waitForData(
    condition: () => boolean,
    timeout = 2500,
    interval = 100,
  ): Promise<void> {
    return new Promise((resolve) => {
      const start = Date.now();
      const check = () => {
        if (condition()) {
          resolve();
        } else if (Date.now() - start > timeout) {
          resolve(); // timeout
        } else {
          setTimeout(check, interval);
        }
      };
      check();
    });
  }

  // ==================== LOCATION ====================
  loadProvinces(): void {
    this.locationApi.provinces().subscribe({
      next: (res: any) => {
        this.provinces = res?.provinces || res?.result || res || [];
        console.log('✅ Provinces loaded:', this.provinces.length);
      },
      error: () => this.notify.error('Không tải được tỉnh/thành'),
    });
  }

  loadDistricts(provinceCode: any): void {
    if (!provinceCode) {
      this.districts = [];
      this.wards = [];
      this.checkoutForm.get('district')?.disable({ emitEvent: false });
      this.checkoutForm.get('ward')?.disable({ emitEvent: false });
      return;
    }

    this.districts = [];
    this.wards = [];
    this.checkoutForm.patchValue(
      { district: '', ward: '' },
      { emitEvent: false },
    );

    this.locationApi.districts(provinceCode).subscribe({
      next: (res: any) => {
        this.districts = res?.districts || res?.result || [];
        console.log('✅ Districts loaded:', this.districts.length);

        if (this.districts.length > 0) {
          this.enableDistrictField();
        }
      },
      error: () => this.notify.error('Không tải được quận/huyện'),
    });
  }

  loadWards(districtCode: any): void {
    if (!districtCode) {
      this.wards = [];
      this.checkoutForm.get('ward')?.disable({ emitEvent: false });
      return;
    }

    this.wards = [];
    this.checkoutForm.patchValue({ ward: '' }, { emitEvent: false });

    this.locationApi.wards(districtCode).subscribe({
      next: (res: any) => {
        this.wards = res?.wards || res?.result || [];
        console.log('✅ Wards loaded:', this.wards.length);

        if (this.wards.length > 0) {
          this.enableWardField();
        }
      },
      error: () => this.notify.error('Không tải được phường/xã'),
    });
  }

  private listenLocationChanges(): void {
    this.sub.add(
      this.checkoutForm.get('province')!.valueChanges.subscribe((code) => {
        this.loadDistricts(code);
      }),
    );

    this.sub.add(
      this.checkoutForm.get('district')!.valueChanges.subscribe((code) => {
        this.loadWards(code);
      }),
    );
  }

  // ==================== CART & SUMMARY ====================
  private bindCart(): void {
    this.isLoading = true;
    this.sub.add(
      this.cartService.items$.subscribe((items) => {
        this.cartItems = items || [];
        this.calculateSummary();
        this.isLoading = false;
      }),
    );

    this.cartService.refreshCart().subscribe();
  }

  private calculateSummary(): void {
    const subtotal = this.cartItems.reduce(
      (s, i) => s + (i.price || 0) * (i.quantity || 0),
      0,
    );

    const shippingFee =
      this.shippingMethods.find((m) => m.id === this.selectedShippingMethodId)
        ?.fee ?? 0;

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

  // ==================== PAYMENT & ORDER ====================
  onPaymentMethodChange(paymentMethod: CheckoutRequestPaymentMethodEnum): void {
    this.selectedPaymentMethod = paymentMethod;
  }

  onShippingMethodChange(methodId: 'STANDARD' | 'EXPRESS'): void {
    this.selectedShippingMethodId = methodId;
    this.calculateSummary();
  }

  applyDiscount(): void {
    this.discountApplied = true;
    this.discountPercentage = 0; // bạn có thể cập nhật logic sau
  }

  removeDiscount(): void {
    this.discountApplied = false;
    this.discountPercentage = 0;
  }

  submitOrder(): void {
    this.formTouched = true;

    // Ngăn click nhiều lần
    if (
      this.isSubmitting ||
      this.checkoutForm.invalid ||
      this.cartItems.length === 0
    ) {
      if (this.checkoutForm.invalid) {
        this.notify.error('Vui lòng nhập đầy đủ thông tin bắt buộc');
      }
      return;
    }

    if (!this.selectedPaymentMethod) {
      this.notify.error('Vui lòng chọn phương thức thanh toán');
      return;
    }

    this.isSubmitting = true;

    const ids = this.cartService.getCartIdentifiers();
    const isGuest = !ids.userId;

    const checkoutItems: CheckoutItemRequest[] = this.cartItems.map((i) => ({
      productId: i.productId!,
      variantId: i.variantId!,
      quantity: i.quantity!,
    }));

    const formValue = this.checkoutForm.value;

    const payload: CheckoutRequest = {
      ...(isGuest ? { guestId: ids.guestId } : {}),
      guestName: isGuest ? formValue.name : undefined,
      guestEmail: isGuest ? formValue.email : undefined,
      guestPhone: isGuest ? formValue.phone : undefined,

      shippingAddress: formValue.addressDetail,
      shippingProvince: formValue.province,
      shippingDistrict: formValue.district,
      shippingWard: formValue.ward,
      shippingNote: formValue.note,

      paymentMethod: this.selectedPaymentMethod!,
      shippingMethod: this.selectedShippingMethodId,
      items: checkoutItems,
    };

    this.orderApi.checkout(payload).subscribe({
      next: (res: any) => {
        const order = res;
        console.log('ORDER ID', order.id);

        if (
          this.selectedPaymentMethod === CheckoutRequestPaymentMethodEnum.VNPAY
        ) {
          this.createPayment(order.id);
        } else {
          this.completeOrder();
        }
      },
      error: (err) => {
        console.error(err);
        this.notify.error('Checkout thất bại. Vui lòng thử lại!');
        this.isSubmitting = false;
      },
    });
  }

  private createPayment(orderId: number): void {
    const ids = this.cartService.getCartIdentifiers();
    const isGuest = !ids.userId;

    if (isGuest) {
      const payload = {
        orderId,
        guestId: ids.guestId!,
        method: CheckoutRequestPaymentMethodEnum.VNPAY,
      };
      this.paymentApi.guestPayment(payload).subscribe({
        next: (res) => this.handlePaymentResponse(res),
        error: (err) => {
          console.error(err);
          this.notify.error('Tạo link thanh toán thất bại');
          this.isSubmitting = false;
        },
      });
    } else {
      const payload = { orderId, method: 'VNPAY' };
      this.paymentApi.userPayment(payload).subscribe({
        next: (res) => this.handlePaymentResponse(res),
        error: (err) => {
          console.error(err);
          this.notify.error('Tạo link thanh toán thất bại');
          this.isSubmitting = false;
        },
      });
    }
  }

  private handlePaymentResponse(res: any): void {
    const data = res;
    if (!data?.paymentUrl) {
      this.notify.error('Không nhận được link thanh toán');
      this.isSubmitting = false;
      return;
    }

    this.cartService.clearCart().subscribe();
    window.location.href = data.paymentUrl;
  }

  private handlePaymentError(): void {
    this.notify.error('Tạo thanh toán thất bại');
    this.isSubmitting = false;
  }

  private completeOrder(): void {
    this.notify.success('Đặt hàng thành công!');
    this.cartService.clearCart().subscribe();
  }
}

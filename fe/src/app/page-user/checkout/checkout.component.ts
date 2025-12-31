import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';

import { CartService } from '../cart/cart.service';
import { CartItemResponse } from 'src/app/api/user/model/cartItemResponse';
import { NotificationService } from 'src/app/shared/services/notification.service';
import { HttpClient } from '@angular/common/http';
import { LocationControllerService } from 'src/app/api/user/api/locationController.service';
import { PaymentUserControllerService } from 'src/app/api/user/api/paymentUserController.service';
import { OrderUserControllerService } from 'src/app/api/user/api/orderUserController.service';
import { CheckoutRequest } from 'src/app/api/user/model/checkoutRequest';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss'],
})
export class CheckoutComponent implements OnInit, OnDestroy {
  checkoutForm!: FormGroup;
  private sub = new Subscription();

  /* ===== STATE ===== */
  isLoading = false;
  isSubmitting = false;
  isProcessingPayment = false;
  formTouched = false;

  /* ===== CART ===== */
  cartItems: CartItemResponse[] = [];
  get cartItemsCount(): number {
    return this.cartItems.reduce((s, i) => s + (i.quantity || 0), 0);
  }

  /* ===== SHIPPING ===== */
  shippingMethods = [
    {
      id: 'STANDARD',
      name: 'Giao hàng tiêu chuẩn',
      description: 'Giao trong 3–5 ngày',
      fee: 0,
      estimatedDays: '3–5 ngày',
    },
  ];
  selectedShippingMethodId = 'STANDARD';

  provinces: any[] = [];
  districts: any[] = [];
  wards: any[] = [];

  /* ===== PAYMENT ===== */
  selectedPaymentMethod: 'COD' | 'QR' = 'COD';

  /* ===== DISCOUNT ===== */
  discountCode = '';
  discountApplied = false;
  discountPercentage = 0;

  /* ===== SUMMARY ===== */
  orderSummary = {
    subtotal: 0,
    shipping: 0,
    discount: 0,
    total: 0,
  };

  constructor(
    private fb: FormBuilder,
    private cartService: CartService,
    private orderApi: OrderUserControllerService,
    private paymentApi: PaymentUserControllerService,
    private notify: NotificationService,
    private http: HttpClient,
    private locationApi: LocationControllerService
  ) {}

  private readonly PROVINCE_API = 'https://provinces.open-api.vn/api';

  ngOnInit(): void {
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

    this.loadProvinces();

    // Province change
    this.checkoutForm.get('province')!.valueChanges.subscribe((code) => {
      this.loadDistricts(code);
    });

    // District change
    this.checkoutForm.get('district')!.valueChanges.subscribe((code) => {
      this.loadWards(code);
    });

    this.sub.add(
      this.cartService.items$.subscribe((items) => {
        this.cartItems = items || [];
        this.calculateSummary();
      })
    );
  }

  loadProvinces(): void {
    this.locationApi.provinces().subscribe({
      next: async (res: any) => {
        if (res instanceof Blob) {
          const text = await res.text();
          this.provinces = JSON.parse(text);
        } else {
          this.provinces = res;
        }
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
        let data: any;

        if (res instanceof Blob) {
          const text = await res.text();
          data = JSON.parse(text);
        } else {
          data = res;
        }

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
        let data: any;

        if (res instanceof Blob) {
          const text = await res.text();
          data = JSON.parse(text);
        } else {
          data = res;
        }

        this.wards = data?.wards || [];
        this.checkoutForm.get('ward')!.enable();
      },
      error: () => this.notify.error('Không tải được phường/xã'),
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }

  /* ================= VALIDATION ================= */
  isFormValid(): boolean {
    return this.checkoutForm.valid && this.cartItems.length > 0;
  }

  getValidationClass(control: string): string {
    const c = this.checkoutForm.get(control);
    if (!this.formTouched || !c) return 'border-gray-300';
    return c.invalid ? 'border-red-500' : 'border-green-500';
  }

  /* ================= SHIPPING ================= */
  onShippingMethodChange(id: string): void {
    this.selectedShippingMethodId = id;
    this.calculateSummary();
  }

  /* ================= PAYMENT ================= */
  onPaymentMethodChange(method: 'COD' | 'QR'): void {
    this.selectedPaymentMethod = method;
  }

  /* ================= SUMMARY ================= */
  calculateSummary(): void {
    const subtotal = this.cartItems.reduce(
      (s, i) => s + (i.price || 0) * (i.quantity || 0),
      0
    );

    this.orderSummary = {
      subtotal,
      shipping: 0,
      discount: this.discountApplied
        ? (subtotal * this.discountPercentage) / 100
        : 0,
      total:
        subtotal -
        (this.discountApplied ? (subtotal * this.discountPercentage) / 100 : 0),
    };
  }

  /* ================= DISCOUNT ================= */
  applyDiscount(): void {
    if (this.discountCode === 'SALE10') {
      this.discountPercentage = 10;
      this.discountApplied = true;
      this.calculateSummary();
    }
  }

  removeDiscount(): void {
    this.discountApplied = false;
    this.discountPercentage = 0;
    this.discountCode = '';
    this.calculateSummary();
  }

  /* ================= UTILS ================= */
  formatCurrency(v: number): string {
    return v.toLocaleString('vi-VN', {
      style: 'currency',
      currency: 'VND',
    });
  }

  /* ================= SUBMIT ================= */
  submitOrder(): void {
    this.formTouched = true;
    if (!this.isFormValid()) return;

    this.isSubmitting = true;

    const payload: CheckoutRequest = {
      guestName: this.checkoutForm.value.name,
      guestEmail: this.checkoutForm.value.email,
      guestPhone: this.checkoutForm.value.phone,
      shippingAddress: this.checkoutForm.value.addressDetail,
      shippingProvince: this.checkoutForm.value.province,
      shippingDistrict: this.checkoutForm.value.district,
      shippingWard: this.checkoutForm.value.ward,
      shippingNote: this.checkoutForm.value.note,
      paymentMethod: this.selectedPaymentMethod,
      shippingMethod: this.selectedShippingMethodId,
    };

    this.orderApi.checkout(payload).subscribe({
      next: (order) => {
        if (this.selectedPaymentMethod === 'QR') {
          this.isProcessingPayment = true;
          this.paymentApi
            .createPayment({ orderId: order.id!, method: 'QR' })
            .subscribe((p: any) => {
              window.location.href = p.paymentUrl;
            });
        } else {
          this.notify.success('Đặt hàng thành công!');
          this.isSubmitting = false;
        }
      },
      error: () => {
        this.isSubmitting = false;
        this.notify.error('Checkout thất bại');
      },
    });
  }
}

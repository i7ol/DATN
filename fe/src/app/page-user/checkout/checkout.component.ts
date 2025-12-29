import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CartService } from '../cart/cart.service';
import { CartItemResponse } from 'src/app/api/user/model/cartItemResponse';
import { NotificationService } from 'src/app/shared/services/notification.service';
import { Subscription } from 'rxjs';
interface ShippingMethod {
  id: string;
  name: string;
  description: string;
  fee: number;
  estimatedDays?: string;
}

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.scss'],
})
export class CheckoutComponent implements OnInit {
  checkoutForm: FormGroup;
  formTouched = false;
  isLoading = false;
  isSubmitting = false;
  isProcessingPayment = false;
  cartItems: CartItemResponse[] = [];
  cartItemsCount = 0;
  // cartItemsCount = this.cartItems.reduce((sum, item) => sum + item.quantity, 0);

  // Shipping
  shippingMethods: ShippingMethod[] = [
    {
      id: 'standard',
      name: 'Giao hàng tiêu chuẩn',
      description: '3-5 ngày',
      fee: 30000,
      estimatedDays: '3-5 ngày',
    },
    {
      id: 'express',
      name: 'Giao hàng nhanh',
      description: '1-2 ngày',
      fee: 50000,
      estimatedDays: '1-2 ngày',
    },
  ];
  selectedShippingMethodId = 'standard';

  // Payment
  selectedPaymentMethod: 'COD' | 'QR' = 'COD';

  // Address data
  provinces = [
    { code: 'HN', name: 'Hà Nội' },
    { code: 'HCM', name: 'TP. Hồ Chí Minh' },
  ];
  districts = [];
  wards = [];

  // Discount
  discountCode = '';
  discountApplied = false;
  discountPercentage = 0;

  // Order summary
  orderSummary = {
    subtotal: 0,
    shipping: 0,
    discount: 0,
    total: 0,
  };

  constructor(
    private fb: FormBuilder,
    private cartService: CartService,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.subscribeCart();
  }
  private initForm() {
    this.checkoutForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      name: ['', Validators.required],
      phone: ['', Validators.required],
      province: ['', Validators.required],
      district: ['', Validators.required],
      ward: ['', Validators.required],
      addressDetail: ['', Validators.required],
      note: [''],
    });
  }

  private sub = new Subscription();

  private subscribeCart() {
    this.sub.add(
      this.cartService.items$.subscribe((items) => {
        this.cartItems = items;
        this.cartItemsCount = items.reduce(
          (sum, i) => sum + (i.quantity ?? 0),
          0
        );
        this.calculateOrderSummary();
      })
    );
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
  // Form validation helper
  getValidationClass(controlName: string): string {
    const control = this.checkoutForm.get(controlName);
    if (!this.formTouched) return '';
    return control?.invalid ? 'border-red-500' : 'border-green-500';
  }

  isFormValid(): boolean {
    return (
      this.checkoutForm.valid &&
      !!this.selectedShippingMethodId &&
      !!this.selectedPaymentMethod
    );
  }

  // Shipping & Payment
  onShippingMethodChange(id: string) {
    this.selectedShippingMethodId = id;
    this.calculateOrderSummary();
  }

  onPaymentMethodChange(method: 'COD' | 'QR') {
    this.selectedPaymentMethod = method;
  }

  // Address change (mock)
  onProvinceChange() {
    const province = this.checkoutForm.get('province')?.value;
    if (province === 'HN') {
      this.districts = [
        { code: 'Q1', name: 'Quận 1' },
        { code: 'Q2', name: 'Quận 2' },
      ];
    } else if (province === 'HCM') {
      this.districts = [
        { code: 'Q3', name: 'Quận 3' },
        { code: 'Q4', name: 'Quận 4' },
      ];
    } else {
      this.districts = [];
    }
    this.wards = [];
    this.checkoutForm.patchValue({ district: '', ward: '' });
  }

  onDistrictChange() {
    const district = this.checkoutForm.get('district')?.value;
    if (district) {
      this.wards = [
        { code: 'W1', name: 'Phường 1' },
        { code: 'W2', name: 'Phường 2' },
      ];
    } else {
      this.wards = [];
    }
    this.checkoutForm.patchValue({ ward: '' });
  }

  // Discount
  applyDiscount() {
    if (!this.discountCode.trim()) return;
    this.discountApplied = true;

    if (this.discountCode === 'SALE10') this.discountPercentage = 10;
    else if (this.discountCode === 'SALE20') this.discountPercentage = 20;
    else if (this.discountCode === 'FREESHIP') this.discountPercentage = 0;
    else this.discountPercentage = 0;

    this.calculateOrderSummary();
  }

  removeDiscount() {
    this.discountCode = '';
    this.discountApplied = false;
    this.discountPercentage = 0;
    this.calculateOrderSummary();
  }

  // Calculate totals
  calculateOrderSummary() {
    const subtotal = this.cartItems.reduce(
      (sum, item) => sum + item.price * item.quantity,
      0
    );
    const shipping =
      this.shippingMethods.find((m) => m.id === this.selectedShippingMethodId)
        ?.fee || 0;
    let discount = 0;
    if (this.discountApplied) {
      if (this.discountCode === 'FREESHIP') discount = shipping;
      else discount = subtotal * (this.discountPercentage / 100);
    }
    const total = subtotal + shipping - discount;

    this.orderSummary = { subtotal, shipping, discount, total };
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(value);
  }

  // Submit
  submitOrder() {
    this.formTouched = true;
    if (!this.isFormValid()) return;

    this.isSubmitting = true;

    setTimeout(() => {
      this.isSubmitting = false;
      this.notify.success('Đơn hàng của bạn đã được tạo thành công!');
    }, 1500);
  }
}

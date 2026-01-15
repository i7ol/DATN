import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ProductUserControllerService } from 'src/app/api/user/api/productUserController.service';
import { ProductResponse } from 'src/app/api/user/model/productResponse';
import { VariantResponse } from 'src/app/api/user/model/variantResponse';
import { CartService } from 'src/app/page-user/cart/cart.service';
import { NotificationService } from 'src/app/shared/services/notification.service';

@Component({
  selector: 'product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss'],
})
export class ProductDetailComponent implements OnInit {
  /* ================= PRODUCT ================= */
  product: ProductResponse | null = null;
  selectedImage = '';

  /* ================= VARIANT ================= */
  colors: string[] = [];
  sizes: string[] = [];

  selectedColor: string | null = null;
  selectedSize: string | null = null;
  selectedVariant: VariantResponse | null = null;

  isAddingToCart = false;

  constructor(
    private route: ActivatedRoute,
    private productApi: ProductUserControllerService,
    private cartService: CartService,
    private notify: NotificationService
  ) {}

  /* ================= INIT ================= */
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id) return;

    this.productApi.getProduct(id, 'response').subscribe({
      next: (resp) => {
        if (resp.body instanceof Blob) {
          resp.body.text().then((text) => {
            this.mapProduct(JSON.parse(text));
          });
        } else {
          this.mapProduct(resp.body as ProductResponse);
        }
      },
      error: () => this.notify.error('Không tải được sản phẩm'),
    });
  }

  /* ================= ADD TO CART ================= */
  addToCart(): void {
    if (!this.product) return;

    // Nếu đã chọn variant → dùng luôn
    let variant = this.selectedVariant;

    // Nếu chưa chọn nhưng chỉ có 1 variant
    if (!variant && this.product.variants?.length === 1) {
      variant = this.product.variants[0];
    }

    if (!variant?.id) {
      this.notify.error('Vui lòng chọn màu và size');
      return;
    }

    this.isAddingToCart = true;

    this.cartService.addItem(variant.id, 1).subscribe({
      next: () => {
        this.notify.success('Đã thêm vào giỏ hàng');
        this.isAddingToCart = false;
      },
      error: () => {
        this.notify.error('Thêm vào giỏ hàng thất bại');
        this.isAddingToCart = false;
      },
    });
  }

  /* ================= PRODUCT MAP ================= */
  private mapProduct(data: ProductResponse): void {
    this.product = {
      ...data,
      images: data.images || [],
      variants: data.variants || [],
    };

    this.selectedImage =
      this.product.images.length > 0 ? this.product.images[0].url : '';

    this.colors = Array.from(
      new Set(this.product.variants.map((v) => v.color))
    );
  }

  /* ================= VARIANT SELECT ================= */
  selectColor(color: string): void {
    this.selectedColor = color;
    this.selectedSize = null;
    this.selectedVariant = null;

    const availableSizes =
      this.product?.variants
        ?.filter((v) => v.color === color && (v.stock || 0) > 0)
        .map((v) => v.sizeName) || [];

    this.sizes = Array.from(new Set(availableSizes));
  }

  selectSize(size: string): void {
    this.selectedSize = size;

    if (this.selectedColor && this.selectedSize) {
      this.selectedVariant =
        this.product?.variants?.find(
          (v) =>
            v.color === this.selectedColor &&
            v.sizeName === this.selectedSize &&
            (v.stock || 0) > 0
        ) || null;
    }
  }

  /* ================= IMAGE ================= */
  selectImage(url: string): void {
    this.selectedImage = url;
  }

  /* ================= HELPERS ================= */
  isVariantAvailable(color: string, size: string): boolean {
    if (!this.product?.variants) return false;

    const variant = this.product.variants.find(
      (v) => v.color === color && v.sizeName === size
    );

    return (variant?.stock || 0) > 0;
  }

  getCurrentVariant(): VariantResponse | null {
    if (!this.selectedColor || !this.selectedSize) return null;

    return (
      this.product?.variants?.find(
        (v) =>
          v.color === this.selectedColor && v.sizeName === this.selectedSize
      ) || null
    );
  }
}

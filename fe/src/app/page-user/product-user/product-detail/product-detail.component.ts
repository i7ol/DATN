import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  ProductUserControllerService,
  ProductResponse,
  VariantResponse,
} from 'src/app/api/user';
import { CartService } from '../../cart/cart.service';
import { NotificationService } from 'src/app/shared/services/notification.service';
@Component({
  selector: 'product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss'],
})
export class ProductDetailComponent implements OnInit {
  product: ProductResponse | null = null;
  selectedImage = '';

  colors: string[] = [];
  sizes: string[] = [];

  selectedColor: string | null = null;
  selectedSize: string | null = null;
  selectedVariant: VariantResponse | null = null; // Thêm property này

  constructor(
    private route: ActivatedRoute,
    private productApi: ProductUserControllerService,
    private cartService: CartService,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (id) {
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
        error: (err) => console.error('Lỗi load product:', err),
      });
    }
  }

  addToCart() {
    if (!this.product) return;

    // Nếu sản phẩm chỉ có 1 biến thể, lấy luôn variant đó
    const variant: VariantResponse | undefined = this.product.variants?.[0];

    // Nếu người dùng đã chọn màu/sizes thì tìm variant tương ứng
    const selectedVariant =
      this.product.variants?.find(
        (v) =>
          (!this.selectedColor || v.color === this.selectedColor) &&
          (!this.selectedSize || v.sizeName === this.selectedSize)
      ) || variant;

    if (!selectedVariant) {
      this.notify.error('Vui lòng chọn biến thể sản phẩm');
      return;
    }

    this.cartService.addItem(selectedVariant.id!, 1).subscribe({
      next: () => this.notify.success('Đã thêm vào giỏ hàng'),
      error: (err) => console.error('Thêm vào giỏ hàng lỗi:', err),
    });
  }

  private mapProduct(data: ProductResponse) {
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

  selectColor(color: string) {
    this.selectedColor = color;
    this.selectedSize = null; // Reset size khi chọn màu mới
    this.selectedVariant = null; // Reset variant đã chọn

    // Lọc các size có sẵn cho màu đã chọn
    const availableSizes =
      this.product?.variants
        ?.filter((v) => v.color === color && (v.stock || 0) > 0)
        .map((v) => v.sizeName) || [];

    this.sizes = Array.from(new Set(availableSizes));
  }

  selectSize(size: string) {
    this.selectedSize = size;

    // Tìm variant tương ứng với màu và size đã chọn
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

  selectImage(url: string) {
    this.selectedImage = url;
  }

  // Helper method để kiểm tra variant có sẵn hàng
  isVariantAvailable(color: string, size: string): boolean {
    if (!this.product?.variants) return false;

    const variant = this.product.variants.find(
      (v) => v.color === color && v.sizeName === size
    );

    return variant ? (variant.stock || 0) > 0 : false;
  }

  // Helper method để lấy variant hiện tại
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

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import {
  ProductUserControllerService,
  ProductResponse,
  VariantResponse,
} from 'src/app/api/user';
import { CartService } from '../../cart/cart.service';
@Component({
  selector: 'product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss'],
})
export class ProductDetailComponent implements OnInit {
  product: ProductResponse | null = null;
  selectedImage: string = '';

  colors: string[] = [];
  sizes: string[] = [];

  selectedColor: string | null = null;
  selectedSize: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private productApi: ProductUserControllerService,
    private sanitizer: DomSanitizer,
    private cartService: CartService
  ) {}

  safeUrl(url: string): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl(url);
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : null;

    if (id !== null) {
      this.productApi.getProduct(id, 'response').subscribe({
        next: (resp) => {
          if (resp.body instanceof Blob) {
            resp.body.text().then((text) => {
              const data: ProductResponse = JSON.parse(text);
              this.mapProduct(data);
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

    this.cartService.addItem(this.product.id!, 1).subscribe(() => {
      alert('Đã thêm vào giỏ hàng');
    });
  }

  private mapProduct(data: ProductResponse) {
    this.product = {
      ...data,
      images: data.images || [],
      variants: data.variants || [],
    };

    this.selectedImage =
      this.product.images && this.product.images.length > 0
        ? this.product.images[0].url
        : '';

    // Lấy list màu
    this.colors = Array.from(
      new Set(this.product.variants?.map((v: VariantResponse) => v.color))
    );

    // Lấy list size ALL (ban đầu)
    this.sizes = Array.from(
      new Set(this.product.variants?.map((v: VariantResponse) => v.sizeName))
    );
  }

  selectColor(color: string) {
    this.selectedColor = color;

    // Lọc size theo màu
    this.sizes = Array.from(
      new Set(
        this.product?.variants
          ?.filter((v) => v.color === color)
          .map((v) => v.sizeName)
      )
    );

    this.selectedSize = null; // Reset size
  }

  selectSize(size: string) {
    this.selectedSize = size;
  }

  selectImage(url: string) {
    this.selectedImage = url;
  }
}

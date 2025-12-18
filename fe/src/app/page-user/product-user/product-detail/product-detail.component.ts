import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
  selectedImage = '';

  colors: string[] = [];
  sizes: string[] = [];

  selectedColor: string | null = null;
  selectedSize: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private productApi: ProductUserControllerService,
    private cartService: CartService
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

    // ✅ image.url đã là full URL
    this.selectedImage =
      this.product.images.length > 0 ? this.product.images[0].url : '';

    this.colors = Array.from(
      new Set(this.product.variants.map((v) => v.color))
    );

    this.sizes = Array.from(
      new Set(this.product.variants.map((v) => v.sizeName))
    );
  }

  selectColor(color: string) {
    this.selectedColor = color;
    this.sizes = Array.from(
      new Set(
        this.product?.variants
          ?.filter((v) => v.color === color)
          .map((v) => v.sizeName)
      )
    );
    this.selectedSize = null;
  }

  selectSize(size: string) {
    this.selectedSize = size;
  }

  selectImage(url: string) {
    this.selectedImage = url;
  }
}

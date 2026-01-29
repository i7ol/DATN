import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ProductUserControllerService } from 'src/app/api/user/api/productUserController.service';
import { PageProductResponse } from 'src/app/api/user/model/pageProductResponse';
import { ProductResponse } from 'src/app/api/user/model/productResponse';

import { CartService } from 'src/app/page-user/cart/cart.service';
import { NotificationService } from 'src/app/shared/services/notification.service';

@Component({
  selector: 'product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss'],
})
export class ProductListComponent implements OnInit {
  /* ================= STATE ================= */
  products: ProductResponse[] = [];
  loading = false;

  /* ================= PAGINATION ================= */
  currentPage = 0;
  pageSize = 20;
  totalItems = 0;

  constructor(
    private productApi: ProductUserControllerService,
    private router: Router,
    private cartService: CartService,
    private notify: NotificationService,
  ) {}

  /* ================= INIT ================= */
  ngOnInit(): void {
    this.loadProducts();
  }

  /* ================= ADD TO CART ================= */

  /**
   * Click từ button "Thêm vào giỏ"
   */
  addToCart(product: ProductResponse): void {
    if (!product.variants || product.variants.length === 0) {
      this.handleNoVariant(product);
      return;
    }

    // Có nhiều hơn 1 variant → bắt buộc vào chi tiết
    if (product.variants.length > 1) {
      this.viewProductDetail(product);
      return;
    }

    // Chỉ có 1 variant → add luôn
    this.addSingleVariantToCart(product);
  }

  /**
   * Thêm vào giỏ hàng khi sản phẩm chỉ có 1 biến thể
   */
  private addSingleVariantToCart(product: ProductResponse): void {
    const variant = product.variants?.[0];

    if (!variant?.id) {
      this.notify.error('Biến thể sản phẩm không hợp lệ');
      return;
    }

    if ((variant.stock ?? 0) <= 0) {
      this.notify.info('Sản phẩm đã hết hàng');
      return;
    }

    this.cartService.addItem(variant.id, 1).subscribe({
      next: () => {
        this.notify.success('Đã thêm vào giỏ hàng');
      },
      error: (err) => this.handleAddToCartError(err),
    });
  }

  private handleAddToCartError(err: any): void {
    console.error(err);

    switch (err?.status) {
      case 400:
        this.notify.error('Dữ liệu không hợp lệ');
        break;
      case 404:
        this.notify.error('Sản phẩm hoặc biến thể không tồn tại');
        break;
      case 401:
      case 403:
        this.notify.info('Vui lòng đăng nhập');
        break;
      default:
        this.notify.error('Có lỗi xảy ra, vui lòng thử lại');
    }
  }

  /* ================= NAVIGATION ================= */

  viewProductDetail(product: ProductResponse): void {
    this.router.navigate(['/product', product.id]);
  }

  handleNoVariant(product: ProductResponse): void {
    this.notify.info('Sản phẩm này hiện không có biến thể nào');
  }

  /* ================= LOAD PRODUCTS ================= */

  loadProducts(page: number = 0, size: number = this.pageSize): void {
    this.loading = true;

    this.productApi.getAllProducts(page, size, 'response').subscribe({
      next: (resp: any) => {
        const handleBody = (body: any) => {
          const data: PageProductResponse = body;

          this.products = data.content || [];
          this.totalItems = data.totalElements || this.products.length;
          this.currentPage = data.number || 0;
          this.pageSize = data.size || this.pageSize;

          this.loading = false;
        };

        if (resp.body instanceof Blob) {
          resp.body.text().then((text: string) => {
            handleBody(JSON.parse(text));
          });
        } else {
          handleBody(resp.body);
        }
      },
      error: (err) => {
        console.error('Load products error:', err);
        this.loading = false;

        if (err.status === 404) {
          this.notify.error('Không tìm thấy API sản phẩm');
        } else {
          this.notify.error('Không tải được danh sách sản phẩm');
        }
      },
    });
  }

  /* ================= PAGINATION ================= */

  get totalPages(): number {
    return Math.ceil(this.totalItems / this.pageSize);
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.loadProducts(this.currentPage - 1);
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.loadProducts(this.currentPage + 1);
    }
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.loadProducts(page);
    }
  }

  /* ================= HELPERS ================= */

  hasMultipleVariants(product: ProductResponse): boolean {
    return (product.variants?.length || 0) > 1;
  }

  hasSingleVariant(product: ProductResponse): boolean {
    return (product.variants?.length || 0) === 1;
  }

  hasStock(product: ProductResponse): boolean {
    const variant = product.variants?.[0];
    return !!variant && (variant.stock ?? 0) > 0;
  }

  isOutOfStock(product: ProductResponse): boolean {
    return !this.hasStock(product);
  }
}

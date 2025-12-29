import { Component, OnInit } from '@angular/core';
import {
  ProductResponse,
  PageProductResponse,
  ProductUserControllerService,
} from 'src/app/api/user';
import { CartService } from '../../cart/cart.service';
import { Router } from '@angular/router';

import { NotificationService } from 'src/app/shared/services/notification.service';
@Component({
  selector: 'product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss'],
})
export class ProductListComponent implements OnInit {
  products: ProductResponse[] = [];
  loading = false;

  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalItems = 0;

  constructor(
    private productApi: ProductUserControllerService,
    private router: Router,
    private cartService: CartService,
    private notify: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  /**
   * Thêm vào giỏ hàng khi sản phẩm chỉ có 1 biến thể
   */
  addToCart(product: ProductResponse) {
    if (!product.variants || product.variants.length !== 1) {
      this.viewProductDetail(product);
      return;
    }

    const variant = product.variants[0];

    if (!variant || variant.id == null) {
      this.notify.error('Biến thể sản phẩm không hợp lệ!');
      return;
    }

    this.cartService.addItem(variant.id, 1).subscribe({
      next: () => this.notify.success('Đã thêm vào giỏ hàng'),
      error: (err) => this.handleAddToCartError(err),
    });
  }

  private handleAddToCartError(err: any) {
    console.error(err);

    switch (err.status) {
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

  onAddToCartClick(product: ProductResponse) {
    if (!product.variants || product.variants.length === 0) {
      this.handleNoVariant(product);
      return;
    }

    if (product.variants.length === 1) {
      this.addToCart(product);
      return;
    }

    this.viewProductDetail(product);
  }

  /**
   * Xem chi tiết sản phẩm (dùng cho sản phẩm có nhiều biến thể)
   */
  viewProductDetail(product: ProductResponse) {
    this.router.navigate(['/product', product.id]);
  }

  /**
   * Xử lý khi sản phẩm không có biến thể
   */
  handleNoVariant(product: ProductResponse) {
    this.notify.info('Sản phẩm này hiện không có biến thể nào!');
  }

  /** ---------------------------
   * Load danh sách sản phẩm
   * --------------------------- */
  loadProducts(page: number = 0, size: number = this.pageSize) {
    this.loading = true;

    this.productApi.getAllProducts(page, size, 'response').subscribe({
      next: (resp: any) => {
        const onBody = (body: any) => {
          const data: PageProductResponse = body;

          this.products = data.content || [];

          // Debug: Kiểm tra số lượng biến thể của từng sản phẩm
          this.products.forEach((product, index) => {
            console.log(
              `Sản phẩm ${index}: ${product.name} - Số biến thể: ${
                product.variants?.length || 0
              }`
            );
          });

          this.totalItems = data.totalElements || this.products.length;
          this.currentPage = data.number || 0;
          this.pageSize = data.size || this.pageSize;
          this.loading = false;
        };

        if (resp.body instanceof Blob) {
          resp.body.text().then((text: string) => onBody(JSON.parse(text)));
        } else {
          onBody(resp.body);
        }
      },
      error: (err) => {
        console.error('Load products error:', err);
        this.loading = false;

        // Specific error handling
        if (err.status === 404) {
          console.error('API endpoint not found. Please check the API URL.');
        }
      },
    });
  }

  /** ---------------------------
   * Phân trang
   * --------------------------- */
  get totalPages(): number {
    return Math.ceil(this.totalItems / this.pageSize);
  }

  prevPage() {
    if (this.currentPage > 0) this.loadProducts(this.currentPage - 1);
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1)
      this.loadProducts(this.currentPage + 1);
  }

  goToPage(page: number) {
    if (page >= 0 && page < this.totalPages) this.loadProducts(page);
  }

  /**
   * Kiểm tra sản phẩm có nhiều hơn 1 biến thể
   */
  hasMultipleVariants(product: ProductResponse): boolean {
    return (product.variants?.length || 0) > 1;
  }

  /**
   * Kiểm tra sản phẩm có đúng 1 biến thể
   */
  hasSingleVariant(product: ProductResponse): boolean {
    return (product.variants?.length || 0) === 1;
  }
}

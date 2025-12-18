import { Component, OnInit } from '@angular/core';

import {
  ProductResponse,
  PageProductResponse,
  ProductUserControllerService,
} from 'src/app/api/user';
import { CartService } from '../../cart/cart.service';
import { Router } from '@angular/router';

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
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  addToCart(product: ProductResponse) {
    this.cartService.addItem(product.id!, 1).subscribe(() => {
      alert('Đã thêm vào giỏ hàng');
    });
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
      },
    });
  }

  /** ---------------------------
   * Pagination
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

  viewProductDetail(product: ProductResponse) {
    this.router.navigate(['/product', product.id]);
  }
}

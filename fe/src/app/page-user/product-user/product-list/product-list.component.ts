import { Component, OnInit, Inject } from '@angular/core';
import {
  ProductResponse,
  PageProductResponse,
  ProductUserControllerService,
} from 'src/app/api/user';
import { Router } from '@angular/router';

@Component({
  selector: 'product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss'],
})
export class ProductListComponent implements OnInit {
  products: ProductResponse[] = [];
  loading = false;

  selectedProduct: ProductResponse | null = null;
  showModal = false;

  // Pagination
  currentPage = 0;
  pageSize = 20;
  totalItems = 0;

  private MINIO_URL = 'http://localhost:9000/shop'; // URL MinIO của bạn

  constructor(
    private productApi: ProductUserControllerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  /** ---------------------------
   * Chuẩn hóa URL ảnh
   * --------------------------- */
  private mapImageUrls(product: ProductResponse): ProductResponse {
    if (product.images) {
      product.images = product.images.map((img: any) => ({
        ...img,
        url: img.url
          ? img.url // API trả full URL
          : img.path
          ? `${this.MINIO_URL}/${img.path}` // API chỉ trả path → build URL
          : '',
      }));
    }
    return product;
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

          this.products = (data.content || []).map((p) => this.mapImageUrls(p));

          this.totalItems = data.totalElements || this.products.length;
          this.currentPage = data.number || 0;
          this.pageSize = data.size || this.pageSize;
          this.loading = false;
        };

        // Trường hợp backend trả Blob
        if (resp.body instanceof Blob) {
          resp.body.text().then((text: string) => {
            try {
              const json = JSON.parse(text);
              onBody(json);
            } catch (e) {
              console.error('Parse JSON failed:', e);
              this.loading = false;
            }
          });
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

  get totalPagesArray(): number[] {
    return Array(this.totalPages);
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

  /** ---------------------------
   * Xem chi tiết sản phẩm
   * --------------------------- */
  viewProductDetail(product: ProductResponse) {
    this.router.navigate(['/product', product.id]);
  }

  closeModal() {
    this.showModal = false;
    this.selectedProduct = null;
  }

  addToCart(product: ProductResponse) {
    console.log('Thêm vào giỏ hàng:', product);
  }
}

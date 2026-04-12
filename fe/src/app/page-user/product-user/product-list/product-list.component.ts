import { ProductUserControllerService } from 'src/app/api/user/api/productUserController.service';
import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CarouselModule } from 'ngx-owl-carousel-o';
import { PageProductResponse } from 'src/app/api/user/model/pageProductResponse';
import { ProductResponse } from 'src/app/api/user/model/productResponse';

import { CartService } from 'src/app/page-user/cart/cart.service';
import { NotificationService } from 'src/app/shared/services/notification.service';
import { Pageable } from 'src/app/api/admin/model/models';

@Component({
  selector: 'product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss'],
})
export class ProductListComponent implements OnInit {
  /* ================= STATE ================= */
  products: ProductResponse[] = [];
  loading = false;

  keyword: string = '';
  isSearchMode = false;

  /* ================= PAGINATION ================= */
  currentPage = 0;
  pageSize = 20;
  totalItems = 0;

  constructor(
    private productApi: ProductUserControllerService,
    private router: Router,
    private route: ActivatedRoute,
    private cartService: CartService,
    private notify: NotificationService,
  ) {}

  /* ================= INIT ================= */
  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      this.keyword = params['keyword'] || '';
      this.isSearchMode = !!this.keyword;

      this.currentPage = 0;
      this.loadProducts();
    });
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

  loadProducts(page: number = 0): void {
    this.loading = true;

    if (this.isSearchMode && this.keyword) {
      // Tìm kiếm theo keyword
      this.productApi
        .search(
          { page: page, size: this.pageSize } as Pageable,
          this.keyword,
          undefined,
        )
        .subscribe({
          next: (response: PageProductResponse) => {
            this.handleProductResponse(response);
          },
          error: (err) => this.handleError(err),
        });
    } else {
      // Load tất cả sản phẩm
      this.productApi
        .getAllProducts(page, this.pageSize, 'response')
        .subscribe({
          next: (resp: any) => this.handleHttpResponse(resp),
          error: (err) => this.handleError(err),
        });
    }
  }
  private handleProductResponse(data: PageProductResponse) {
    this.products = data.content || [];
    this.totalItems = data.totalElements || 0;
    this.currentPage = data.number || 0;
    this.loading = false;
  }

  private handleHttpResponse(resp: any) {
    const body = resp.body || resp;
    this.handleProductResponse(body);
  }

  private handleError(err: any) {
    console.error(err);
    this.loading = false;
    this.notify.error('Không tải được dữ liệu');
  }

  /* ================= PAGINATION ================= */

  get totalPages(): number {
    return Math.ceil(this.totalItems / this.pageSize) || 1;
  }
  nextPage() {
    if (this.currentPage < Math.ceil(this.totalItems / this.pageSize) - 1) {
      this.currentPage++;
      this.loadProducts(this.currentPage);
    }
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadProducts(this.currentPage);
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
  banners = [
    {
      id: 1,
      image: 'assets/img/banner.webp',
      title: 'Collection Mới 2026',
      subtitle: 'Streetwear chính hãng',
    },
    {
      id: 2,
      image: 'assets/img/banner2.webp',
      title: 'Sale lên đến 50%',
      subtitle: 'Chỉ trong tuần này',
    },
    {
      id: 3,
      image: 'assets/img/banner3.webp',
      title: 'Clownz x Loudzone',
      subtitle: 'Limited Edition',
    },
  ];

  carouselOptions = {
    loop: true,
    mouseDrag: true,
    items: 1,
    touchDrag: true,
    pullDrag: false,
    dots: true,
    navSpeed: 700,
    autoplay: true,
    center: true,
    autoplayTimeout: 4000,
    autoplayHoverPause: true,
    navText: ['‹', '›'],
    responsive: {
      0: { items: 1 },
      768: { items: 1 },
      1024: { items: 1 },
    },
    nav: true,
  };

  // Method click banner (tùy chọn)
  viewBanner(banner: any) {
    console.log('Clicked banner:', banner);
    // this.router.navigate(['/collection', banner.id]); // nếu bạn có route collection
  }
}

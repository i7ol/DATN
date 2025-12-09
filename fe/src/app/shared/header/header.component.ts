import { Component, HostListener, OnInit, OnDestroy } from '@angular/core';
import { CartService } from 'src/app/page-user/cart/cart.service';
import { Observable } from 'rxjs';
import { CartItemResponse } from 'src/app/api/user';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit, OnDestroy {
  isScrolled = false;
  isCartHover = false;

  // Biến để lưu timeout - QUAN TRỌNG
  private hideCartTimeout: any = null;

  cartCount$!: Observable<number>;
  cartItems$!: Observable<CartItemResponse[]>;
  totalPrice$!: Observable<number>;

  constructor(private cartService: CartService) {}

  ngOnInit(): void {
    this.cartCount$ = this.cartService.totalQuantity$;
    this.cartItems$ = this.cartService.items$;
    this.totalPrice$ = this.cartService.totalPrice$;
  }

  ngOnDestroy(): void {
    // Dọn dẹp timeout khi component bị hủy
    if (this.hideCartTimeout) {
      clearTimeout(this.hideCartTimeout);
    }
  }

  @HostListener('window:scroll')
  onScroll() {
    this.isScrolled = window.scrollY > 10;
  }

  // Khi chuột vào nút Giỏ hàng
  onCartMouseEnter(): void {
    // Hủy timeout ẩn trước đó (nếu có)
    if (this.hideCartTimeout) {
      clearTimeout(this.hideCartTimeout);
      this.hideCartTimeout = null;
    }

    // Hiển thị dropdown ngay lập tức
    this.isCartHover = true;
  }

  // Khi chuột rời khỏi nút Giỏ hàng
  onCartMouseLeave(): void {
    // Tạo độ trễ 300ms trước khi ẩn dropdown
    // Điều này cho phép người dùng có thời gian di chuột vào dropdown
    this.hideCartTimeout = setTimeout(() => {
      this.isCartHover = false;
    }, 300);
  }

  // Khi chuột vào dropdown
  onDropdownMouseEnter(): void {
    // Hủy timeout ẩn (nếu có)
    // Điều này giữ dropdown mở khi người dùng đã di vào được dropdown
    if (this.hideCartTimeout) {
      clearTimeout(this.hideCartTimeout);
      this.hideCartTimeout = null;
    }

    // Đảm bảo dropdown vẫn hiển thị
    this.isCartHover = true;
  }

  // Khi chuột rời khỏi dropdown
  onDropdownMouseLeave(): void {
    // Khi người dùng rời khỏi dropdown, ẩn nó sau 150ms
    // Thời gian ngắn hơn vì người dùng đã rời đi thực sự
    this.hideCartTimeout = setTimeout(() => {
      this.isCartHover = false;
    }, 150);
  }
}

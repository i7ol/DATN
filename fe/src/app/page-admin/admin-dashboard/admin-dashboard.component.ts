import { Component, OnInit } from '@angular/core';

import { PaymentAdminProxyControllerService } from 'src/app/api/admin/api/paymentAdminProxyController.service';

import { ShippingAdminControllerService } from 'src/app/api/admin/api/shippingAdminController.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit {
  stats = {
    totalOrders: 0,
    pendingOrders: 0,
    totalPayments: 0,
    pendingPayments: 0,
    totalShippings: 0,
    processingShippings: 0,
    revenue: 0,
  };

  recentOrders: any[] = [];
  recentPayments: any[] = [];
  recentShippings: any[] = [];

  constructor(
    private paymentService: PaymentAdminProxyControllerService,
    private shippingService: ShippingAdminControllerService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    // this.loadOrderStats();
    this.loadPaymentStats();
    this.loadShippingStats();
  }

  loadPaymentStats(): void {
    this.paymentService.getSummary1().subscribe({
      next: (summary: any) => {
        this.stats.totalPayments = summary.totalPayments ?? 0;
        this.stats.pendingPayments = summary.pendingPayments ?? 0;
        this.stats.revenue = summary.totalRevenue ?? 0;
      },
      error: (err) => {
        console.error('Error loading payment summary:', err);
        this.stats.totalPayments = 0;
        this.stats.pendingPayments = 0;
        this.stats.revenue = 0;
      },
    });
  }

  loadPaymentStatsAlternative(): void {
    // Tìm phương thức khác hoặc sử dụng mock data
    // Kiểm tra các phương thức có sẵn
    console.log('Available payment methods:', Object.keys(this.paymentService));

    // Tạm thời set default values
    this.stats.totalPayments = 0;
    this.stats.pendingPayments = 0;
    this.stats.revenue = 0;
  }

  loadShippingStats(): void {
    // Kiểm tra xem service có phương thức getAllShippings không tham số không
    if (this.shippingService.getAllShippings) {
      // Thử gọi không tham số
      (this.shippingService.getAllShippings as any)().subscribe({
        next: (shippings: any[]) => {
          this.stats.totalShippings = shippings.length;
          this.stats.processingShippings = shippings.filter(
            (s: any) => s.status === 'PROCESSING' || s.status === 'SHIPPING'
          ).length;
          // Sắp xếp theo ngày tạo mới nhất
          this.recentShippings = shippings
            .sort(
              (a: any, b: any) =>
                new Date(b.createdAt).getTime() -
                new Date(a.createdAt).getTime()
            )
            .slice(0, 5);
        },
        error: (error) => {
          console.error('Error loading shippings:', error);
        },
      });
    } else {
      console.warn('Shipping service does not have getAllShippings method');
      // Hoặc tìm phương thức khác
      this.loadShippingStatsAlternative();
    }
  }

  loadShippingStatsAlternative(): void {
    // Tìm phương thức khác hoặc sử dụng mock data
    console.log(
      'Available shipping methods:',
      Object.keys(this.shippingService)
    );

    // Tạm thời set default values
    this.stats.totalShippings = 0;
    this.stats.processingShippings = 0;
  }

  // Helper method để format currency
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(amount);
  }
}

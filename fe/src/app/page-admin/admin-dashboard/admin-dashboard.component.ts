import { Component, OnInit, ViewChild } from '@angular/core';
import { OrderAdminControllerService } from 'src/app/api/admin';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit {
  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  currentTab: string = 'overview';
  chartType: 'line' | 'bar' = 'line';

  filterStartDate: string = '';
  filterEndDate: string = '';

  stats = { totalRevenue: 0, monthRevenue: 0, todayRevenue: 0 };
  totalOrders: number = 0;
  totalCustomers: number = 0;
  totalProducts: number = 0;

  topProducts: any[] = [];
  revenueByDate: any[] = [];

  // ==================== CHART 7 NGÀY ====================
  public lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'top' as const },
      tooltip: {
        callbacks: {
          label: (ctx: any) => ctx.raw.toLocaleString('vi-VN') + ' ₫',
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { callback: (v: any) => (v / 1000000).toFixed(1) + 'M' },
      },
    },
  };

  public lineChartType: ChartType = 'line' as const;
  public lineChartData: ChartData<'line'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Doanh thu',
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.08)',
        borderWidth: 3,
        tension: 0.4,
        fill: true,
      },
    ],
  };

  // ==================== CHART THEO KHOẢNG THỜI GIAN ====================
  public revenueRangeChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'top' as const },
      tooltip: {
        callbacks: {
          label: (ctx: any) => ctx.raw.toLocaleString('vi-VN') + ' ₫',
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { callback: (v: any) => (v / 1000000).toFixed(1) + 'M ₫' },
      },
    },
  };

  public revenueRangeChartType: ChartType = 'line' as const;
  public revenueRangeChartData: ChartData<'line' | 'bar'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Doanh thu',
        borderColor: '#10b981',
        backgroundColor: 'rgba(16, 185, 129, 0.1)',
        borderWidth: 3,
        tension: 0.4,
      },
    ],
  };

  constructor(private orderAdminService: OrderAdminControllerService) {}

  ngOnInit() {
    this.loadOverviewData();
    this.loadTopProducts();
    this.initDefaultDateRange();
  }

  private initDefaultDateRange() {
    const today = new Date();
    const ago = new Date(today);
    ago.setDate(today.getDate() - 30);

    this.filterEndDate = today.toISOString().split('T')[0];
    this.filterStartDate = ago.toISOString().split('T')[0];
  }

  // ==================== TỔNG QUAN ====================
  loadOverviewData(): void {
    this.orderAdminService.getRevenueStatistics().subscribe({
      next: (data: any) => {
        this.stats.totalRevenue = data.totalRevenue || 0;
        this.stats.monthRevenue = data.monthRevenue || 0;
        this.stats.todayRevenue = data.todayRevenue || 0;

        this.totalOrders = data.totalOrders || 0;
        this.totalCustomers = data.totalCustomers || 0;
        this.totalProducts = data.totalProducts || 0;

        // Chart 7 ngày
        if (data.last7Days?.length > 0) {
          this.lineChartData.labels = data.last7Days.map((item: any) =>
            new Date(item.date).toLocaleDateString('vi-VN', {
              day: '2-digit',
              month: '2-digit',
            }),
          );
          this.lineChartData.datasets[0].data = data.last7Days.map(
            (item: any) => Number(item.revenue) || 0,
          );
        }
      },
      error: (err) => console.error('Lỗi load overview:', err),
    });
  }

  // ==================== TOP SẢN PHẨM ====================
  loadTopProducts(): void {
    this.orderAdminService.getTopSellingProducts(10).subscribe({
      next: (data) => {
        this.topProducts = data || [];
      },
      error: (err) => console.error('Lỗi load top products:', err),
    });
  }

  // ==================== DOANH THU THEO KHOẢNG THỜI GIAN ====================
  loadRevenueByDateRange(): void {
    if (!this.filterStartDate || !this.filterEndDate) {
      alert('Vui lòng chọn đầy đủ khoảng thời gian!');
      return;
    }

    this.orderAdminService
      .getRevenueByDate(this.filterStartDate, this.filterEndDate)
      .subscribe({
        next: (data: any) => {
          this.revenueByDate = data || [];

          if (data?.length > 0) {
            this.revenueRangeChartData.labels = data.map((item: any) =>
              new Date(item.date).toLocaleDateString('vi-VN', {
                day: '2-digit',
                month: '2-digit',
              }),
            );
            this.revenueRangeChartData.datasets[0].data = data.map(
              (item: any) => Number(item.revenue) || 0,
            );
          } else {
            this.revenueRangeChartData.labels = [];
            this.revenueRangeChartData.datasets[0].data = [];
          }
          this.chart?.update();
        },
        error: (err) => {
          console.error('Lỗi load doanh thu:', err);
          alert('Có lỗi khi tải dữ liệu biểu đồ');
        },
      });
  }

  toggleChartType() {
    this.chartType = this.chartType === 'line' ? 'bar' : 'line';
    this.revenueRangeChartType = this.chartType;

    const dataset = this.revenueRangeChartData.datasets[0];

    if (this.chartType === 'bar') {
      dataset.backgroundColor = 'rgba(16, 185, 129, 0.75)';
      (dataset as any).fill = false;
      (dataset as any).tension = 0;
    } else {
      dataset.backgroundColor = 'rgba(16, 185, 129, 0.1)';
      (dataset as any).fill = true;
      (dataset as any).tension = 0.4;
    }

    this.chart?.update();
  }

  resetDateFilter() {
    this.initDefaultDateRange();
    this.loadRevenueByDateRange();
  }

  changeTab(tab: string) {
    this.currentTab = tab;
    if (tab === 'revenue-date') {
      setTimeout(() => this.loadRevenueByDateRange(), 100);
    }
  }
}

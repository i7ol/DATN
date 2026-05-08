import { Component, OnInit } from '@angular/core';
import { RevenueService } from './revenue.service';
import { ReportService } from './report.service';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit {
  currentTab: string = 'overview';

  // ==================== STATS ====================
  stats = {
    totalRevenue: 0,
    monthRevenue: 0,
    todayRevenue: 0, // giữ lại phòng trường hợp dùng sau
  };

  totalOrders: number = 0;
  totalCustomers: number = 0;
  totalProducts: number = 0;

  // Top Products
  topProducts: any[] = [];

  // Revenue by Date
  revenueByDate: any[] = [];

  // Line Chart
  public lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true, position: 'top' as const },
      tooltip: {
        callbacks: {
          label: (context: any) => context.raw.toLocaleString('vi-VN') + ' ₫',
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: (value: any) => (value / 1000000).toFixed(1) + 'M',
        },
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

  constructor(
    private revenueService: RevenueService,
    private reportService: ReportService,
  ) {}

  ngOnInit(): void {
    this.loadOverviewData();
    this.loadTopProducts();
  }

  // ==================== LOAD OVERVIEW ====================
  loadOverviewData(): void {
    this.revenueService.getRevenueStatistics().subscribe({
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
      error: (err) => console.error('Lỗi load overview', err),
    });
  }

  // ==================== TOP PRODUCTS ====================
  loadTopProducts(): void {
    this.reportService.getTopSellingProducts(10).subscribe({
      next: (data) => (this.topProducts = data || []),
      error: (err) => console.error('Lỗi load top products', err),
    });
  }

  // ==================== REVENUE BY DATE ====================
  loadRevenueByDate(startDate: string, endDate: string): void {
    this.reportService.getRevenueByDate(startDate, endDate).subscribe({
      next: (data) => (this.revenueByDate = data || []),
      error: (err) => console.error('Lỗi load revenue by date', err),
    });
  }

  changeTab(tab: string): void {
    this.currentTab = tab;
    if (tab === 'revenue-date') {
      const end = new Date().toISOString().split('T')[0];
      const start = new Date(Date.now() - 30 * 86400000)
        .toISOString()
        .split('T')[0];
      this.loadRevenueByDate(start, end);
    }
  }
}

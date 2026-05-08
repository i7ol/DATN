import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private baseUrl = environment.apiUrls.shopAdmin;

  constructor(private http: HttpClient) {}

  getTopSellingProducts(limit: number = 10): Observable<any> {
    return this.http.get(
      `${this.baseUrl}/admin/orders/statistics/top-products?limit=${limit}`,
    );
  }

  getRevenueByDate(startDate: string, endDate: string): Observable<any> {
    return this.http.get(
      `${this.baseUrl}/admin/orders/statistics/revenue-by-date?startDate=${startDate}&endDate=${endDate}`,
    );
  }

  getRevenueSummary(startDate: string, endDate: string): Observable<any> {
    return this.http.get(
      `${this.baseUrl}/admin/orders/statistics/summary?startDate=${startDate}&endDate=${endDate}`,
    );
  }
}

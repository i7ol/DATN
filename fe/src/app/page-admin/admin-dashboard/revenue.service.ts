import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';  

@Injectable({
  providedIn: 'root'
})
export class RevenueService {

  private apiUrl = environment.apiUrls.shopAdmin;

  constructor(private http: HttpClient) {}

  getRevenueStatistics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/admin/orders/statistics/revenue`);
  }
}
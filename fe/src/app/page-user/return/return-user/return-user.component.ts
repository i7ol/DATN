import { Component, OnInit } from '@angular/core';
import { ReturnProxyControllerService } from 'src/app/api/user';
import { ReturnResponse } from 'src/app/api/user/model/returnResponse';
import { PageReturnResponse } from 'src/app/api/user/model/pageReturnResponse';
@Component({
  selector: 'app-return-user',
  templateUrl: './return-user.component.html',
  styleUrls: ['./return-user.component.scss'],
})
export class ReturnUserComponent implements OnInit {
  returns: ReturnResponse[] = [];
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;

  constructor(private returnProxyService: ReturnProxyControllerService) {}

  ngOnInit(): void {
    this.loadReturns();
  }

  loadReturns(): void {
    this.returnProxyService
      .getMyReturns(this.currentPage, this.pageSize)
      .subscribe((res: any) => {
        this.returns = res.result?.content || [];
        this.totalPages = res.result?.totalPages || 0;
      });
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.loadReturns();
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'bg-warning';
      case 'APPROVED':
        return 'bg-success';
      case 'REJECTED':
        return 'bg-danger';
      default:
        return 'bg-secondary';
    }
  }
}

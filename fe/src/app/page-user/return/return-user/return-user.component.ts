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
  loading = false;

  constructor(private returnService: ReturnProxyControllerService) {}

  ngOnInit(): void {
    this.loadReturns();
  }

  loadReturns(): void {
    this.loading = true;
    this.returnService.getMyReturns(0, 20).subscribe({
      next: (res: any) => {
        this.returns = res.result?.content || res.content || [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      },
    });
  }

  getStatusClass(status: string): string {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'status-pending';

      case 'APPROVED':
        return 'status-approved';

      case 'REJECTED':
        return 'status-rejected';

      case 'CANCELLED':
        return 'status-completed';

      default:
        return '';
    }
  }
}

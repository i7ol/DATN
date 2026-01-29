import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((err: HttpErrorResponse) => {
        let message = 'Có lỗi xảy ra';

        switch (err.status) {
          case 401:
            message = 'Phiên đăng nhập hết hạn';
            break;
          case 403:
            message = 'Không có quyền truy cập';
            break;
          case 404:
            message = 'Không tìm thấy dữ liệu';
            break;
          case 500:
            message = 'Lỗi hệ thống';
            break;
        }

        return throwError(() => new Error(message));
      })
    );
  }
}

// app/core/interceptors/error.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Đã xảy ra lỗi. Vui lòng thử lại sau.';

        if (error.error instanceof ErrorEvent) {
          // Client-side error
          errorMessage = error.error.message;
        } else {
          // Server-side error
          switch (error.status) {
            case 400:
              errorMessage = error.error?.message || 'Yêu cầu không hợp lệ';
              break;
            case 401:
              errorMessage = 'Phiên đăng nhập đã hết hạn';
              break;
            case 403:
              errorMessage = 'Bạn không có quyền truy cập';
              break;
            case 404:
              errorMessage = 'Không tìm thấy tài nguyên';
              break;
            case 500:
              errorMessage = 'Lỗi máy chủ. Vui lòng thử lại sau';
              break;
          }
        }

        console.error('HTTP Error:', error);
        return throwError(() => new Error(errorMessage));
      })
    );
  }
}

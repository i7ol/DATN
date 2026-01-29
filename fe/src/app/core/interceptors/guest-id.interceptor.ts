import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class GuestIdInterceptor implements HttpInterceptor {
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler,
  ): Observable<HttpEvent<any>> {
    const guestId = localStorage.getItem('guestId');

    if (guestId) {
      req = req.clone({
        setHeaders: {
          'X-Guest-Id': guestId,
        },
      });
    }

    return next.handle(req);
  }
}

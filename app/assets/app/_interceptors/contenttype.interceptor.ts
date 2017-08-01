import {Injectable} from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class ContentTypeInterceptor implements HttpInterceptor {

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (req.method === 'POST' || req.method === 'PUT') {
      // Clone the request to add the new header.
      const newReq = req.clone({ setHeaders: {'Content-Type': 'application/json'} });
      // Pass on the cloned request instead of the original request.
      return next.handle(newReq);
    } else {
      return next.handle(req);
    }
  }
}

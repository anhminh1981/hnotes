import {Injectable} from '@angular/core';
import { HttpEvent, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Get the auth header from the service.
    const authHeader = this.getAuthorizationHeader();

    if (authHeader) {
      // Clone the request to add the new header.
      const authReq = req.clone({ setHeaders: { Authorization: authHeader } });
      // Pass on the cloned request instead of the original request.
      return next.handle(authReq);
    } else {
      return next.handle(req);
    }

  }

  private getAuthorizationHeader() {
    // create authorization header with jwt token
    const currentUser = JSON.parse(localStorage.getItem('currentUser'));
    if (currentUser && currentUser.token) {
      return 'Bearer ' + currentUser.token;
    }
  }

}

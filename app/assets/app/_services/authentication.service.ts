import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import { User } from '../_models/user';

@Injectable()
export class AuthenticationService {

  constructor(private http: HttpClient) { }

  public login(username: string, password: string) {
    return this.http.post<User>('/api/login', { email: username, password: password } )
      .map(user => {
        // login successful if there's a jwt token in the response
        if (user && user.token) {
          // store user details and jwt token in local storage to keep user logged in between page refreshes
          localStorage.setItem('currentUser', JSON.stringify(user));
        }

        return user;
      });
  }

  public logout() {
    // remove user from local storage to log user out
    localStorage.removeItem('currentUser');
  }

}

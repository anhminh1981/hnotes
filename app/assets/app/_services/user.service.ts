import { Injectable } from '@angular/core';
import { Http, Headers, RequestOptions, Response } from '@angular/http';

import { User } from '../_models/user';
import { AuthenticationService } from './authentication.service';

@Injectable()
export class UserService {
    constructor(private http: Http, private authenticationService: AuthenticationService) { }

    public create(user: User) {
        return this.http.post('/api/signup', user,
          this.authenticationService.jwt()).map((response: Response) => response.json());
    }

    public update(user: User) {
        return this.http.put('/api/users', user,
          this.authenticationService.jwt()).map((response: Response) => response.json());
    }

}

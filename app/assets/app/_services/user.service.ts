import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { User } from '../_models/user';
import { AuthenticationService } from './authentication.service';

@Injectable()
export class UserService {
    constructor(private http: HttpClient, private authenticationService: AuthenticationService) { }

    public create(user: User) {
        return this.http.post('/api/signup', user);
    }

    public update(user: User) {
        return this.http.put('/api/users', user);
    }

}

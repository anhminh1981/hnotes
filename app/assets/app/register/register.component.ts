import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AlertService, UserService } from '../_services/index';

@Component({
    templateUrl: './register.component.html',
    styleUrls: [ './register.component.css'],
})

export class RegisterComponent {
    private model: any = {};
    private loading = false;

    constructor(
        private router: Router,
        private userService: UserService,
        private alertService: AlertService) { }

    public register() {
        this.loading = true;
        this.userService.create(this.model)
            .subscribe(
                data => {
                    // set success message and pass true paramater to persist
                    // the message after redirecting to the login page
                    this.alertService.success('Registration successful', true);
                    this.router.navigate(['/notes']);
                },
                error => {
                    this.alertService.error(error);
                    this.loading = false;
                });
    }
}

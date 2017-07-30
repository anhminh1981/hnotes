import { Component, OnInit } from '@angular/core';

import { AlertService } from '../_services/alert.service';

@Component({
    selector: 'alert',
    templateUrl: 'assets/app/_directives/alert.component.html',
    styleUrls: [ 'assets/app/_directives/alert.component.css'],
})

export class AlertComponent {
    private message: any;

    constructor(private alertService: AlertService) { }

    public ngOnInit() {
        this.alertService.getMessage().subscribe(message => { this.message = message; });
    }
}

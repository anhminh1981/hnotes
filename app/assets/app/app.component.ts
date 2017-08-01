import { Component, OnInit, OnDestroy }          from '@angular/core';
import { Subscription }               from 'rxjs/Subscription';

import { NavService }                 from  './_services/index';

@Component({
  selector: 'hnotes-app',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})

export class AppComponent implements OnInit, OnDestroy {
  private menuDeployed: boolean;
  private subscription: Subscription;

  constructor(private navService: NavService) {  }

  public ngOnInit() {
    this.menuDeployed = false;
    this.subscription = this.navService.getObservable()
      .subscribe(value => this.menuDeployed = value);
  }

  public ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  public toggleMenu() {
    this.navService.toggleMenu();
  }
}

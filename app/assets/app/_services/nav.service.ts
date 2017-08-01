import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

@Injectable()
export class NavService {

  private menuDeployed = false;
  private menuDeployedSource = new BehaviorSubject<boolean>(this.menuDeployed);
  private menuDeployed$ = this.menuDeployedSource.asObservable();

  public toggleMenu() {
    this.menuDeployed = ! this.menuDeployed;
    this.menuDeployedSource.next(this.menuDeployed);
  }

  public getObservable() {
    return this.menuDeployed$;
  }
}

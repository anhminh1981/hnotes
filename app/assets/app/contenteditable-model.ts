import {Directive, ElementRef, Input, Output, OnChanges, SimpleChanges} from '@angular/core';
import {EventEmitter} from '@angular/core';

@Directive({
  selector: '[contenteditableModel]',
  host: {
    '(blur)': 'onBlur()',
  },
})
export class ContenteditableModel implements OnChanges {
  @Input('contenteditableModel') private model: any;
  @Output('contenteditableModelChange') private update = new EventEmitter();

  private lastViewModel: any;

  constructor(private elRef: ElementRef) {
  }

  public ngOnChanges(changes: SimpleChanges) {
    console.log(changes);
    if (changes.model.isFirstChange()) {
      this.refreshView();
    }
  }

  public onBlur() {
    const value = this.elRef.nativeElement.innerText;
    this.lastViewModel = value;
    this.update.emit(value);
  }

  private refreshView() {
    this.elRef.nativeElement.innerText = this.model;
  }
}

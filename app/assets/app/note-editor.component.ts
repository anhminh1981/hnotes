import { Component, Input  } from '@angular/core';
import { Router }            from '@angular/router';

import { Note }				 from './note';

@Component({
  selector: 'note',
  templateUrl: 'assets/app/note-editor.component.html',
  styleUrls: [ 'assets/app/note-editor.component.css' ],
})
export class NoteEditorComponent  {

  @Input() public note: Note;

  constructor(
    private router: Router) {
    }

}

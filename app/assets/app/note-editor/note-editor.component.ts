import { Component, Input, OnChanges, SimpleChanges  } from '@angular/core';
import { Router }            from '@angular/router';

import { Note }				 from '../_models/note';
import { NoteService } from '../_services/notes.service';

@Component({
  selector: 'note',
  templateUrl: './note-editor.component.html',
  styleUrls: [ './note-editor.component.css' ],
})
export class NoteEditorComponent implements OnChanges {

  @Input() public noteId: number;

  private note: Note;

  constructor(
    private noteService: NoteService,
    private router: Router) {
  }

  public ngOnChanges(changes: SimpleChanges) {
    console.log('ngOnChanges ' + JSON.stringify(changes));
    this.noteService.getNote(this.noteId).then(note => this.note = note);
  }

  public save() {
    console.log('saving ' + JSON.stringify(this.note));
    this.noteService.update(this.note);
  }
}

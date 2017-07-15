import { Component, OnInit } from '@angular/core';
import { Router }            from '@angular/router';

import { Note }				 from './note';
import { NoteEditorComponent }		from './note-editor.component';

@Component({
  selector: 'notes',
  templateUrl: 'assets/app/notes.component.html',
  styleUrls: [ 'assets/app/notes.component.css' ],
})
export class NotesComponent implements OnInit {
  public notes: Note[];
  public selectedNote: Note;

  constructor(
    private router: Router) { }

  public ngOnInit(): void {
    this.getNotes();
  }

  public onSelect(note: Note): void {
    this.selectedNote = note;
  }

  private getNotes(): void {
    this.notes = [{id: 0, title: 'test note', text: 'lore ipsum'}, {id: 1, title: 'test note 2', text: 'azertyu'} ];
  }
}

import { Component, OnInit } from '@angular/core';
import { Router }            from '@angular/router';

import { Note }				 from './note';
import { NoteEditorComponent }		from './note-editor.component';
import { NoteService } from './notes.service';

@Component({
  selector: 'notes',
  templateUrl: 'assets/app/notes.component.html',
  styleUrls: [ 'assets/app/notes.component.css' ],
})
export class NotesComponent implements OnInit {
  public notes: Note[];
  public selectedNote: Note;

  constructor(
    private noteService: NoteService,
    private router: Router) { }

  public ngOnInit(): void {
    this.getNotes();
  }

  public onSelect(note: Note): void {
    this.selectedNote = note;
  }

  private getNotes(): void {
    this.noteService.getNotes().then(notes => this.notes = notes);
  }
}

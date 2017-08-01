import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router }            from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { Note }				 from '../_models/note';
import { NoteEditorComponent }		from '../note-editor/note-editor.component';
import { NoteService, NavService } from '../_services/index';

@Component({
  templateUrl: './notes.component.html',
  styleUrls: [ './notes.component.css' ],
})
export class NotesComponent implements OnInit, OnDestroy {
  public notes: Note[];
  public selectedNote: Note;
  private menuDeployed: boolean;
  private subscription: Subscription;

  constructor(
    private noteService: NoteService,
    private navService: NavService,
    private router: Router) { }

  public ngOnInit(): void {
    this.getNotes();
    this.subscription = this.navService.getObservable().subscribe(value => this.menuDeployed = value);
  }

  public ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  public onSelect(note: Note): void {
    this.selectedNote = note;
  }

  private getNotes(): void {
    this.noteService.getNotes().then(notes => this.notes = notes);
  }
}

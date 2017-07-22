import './rxjs-extensions';

import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';
import { HttpModule }    from '@angular/http';

import { CKEditorModule } from 'ng2-ckeditor';

import { AppRoutingModule } from './app-routing.module';

import { AppComponent }         from './app.component';
import { NotesComponent }		from './notes/notes.component';
import { NoteEditorComponent }		from './note-editor/note-editor.component';
import { NoteService } from './_services/notes.service';
import { ContenteditableModel } from './_directives/contenteditable-model';
import { AuthGuard } from './_guards/auth.guard';
import { AlertService } from './_services/alert.service';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    AppRoutingModule,
    CKEditorModule,
  ],
  declarations: [
    AppComponent,
    NotesComponent,
    NoteEditorComponent,
    ContenteditableModel,
  ],
  providers: [ NoteService, AlertService, AuthGuard ],
  bootstrap: [ AppComponent ],
})
export class AppModule { }

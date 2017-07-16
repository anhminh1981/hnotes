import './rxjs-extensions';

import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';
import { HttpModule }    from '@angular/http';

import { CKEditorModule } from 'ng2-ckeditor';

import { AppRoutingModule } from './app-routing.module';

import { AppComponent }         from './app.component';
import { NotesComponent }		from './notes.component';
import { NoteEditorComponent }		from './note-editor.component';
import { NoteService } from './notes.service';
import { ContenteditableModel } from './contenteditable-model';

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
  providers: [ NoteService ],
  bootstrap: [ AppComponent ],
})
export class AppModule { }

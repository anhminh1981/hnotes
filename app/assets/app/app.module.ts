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

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    AppRoutingModule,
  ],
  declarations: [
    AppComponent,
    NotesComponent,
    NoteEditorComponent,
  ],
  providers: [  ],
  bootstrap: [ AppComponent ],
})
export class AppModule { }

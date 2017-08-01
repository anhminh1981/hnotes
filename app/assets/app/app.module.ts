import './rxjs-extensions';

import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';
// import { HttpModule }    from '@angular/http';
import { HttpClientModule }    from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';

import { CKEditorModule } from 'ng2-ckeditor';

import { AppRoutingModule } from './app-routing.module';

import { AppComponent }         from './app.component';
import { NotesComponent }		from './notes/notes.component';
import { NoteEditorComponent }		from './note-editor/note-editor.component';
import { NoteService } from './_services/notes.service';
import { ContenteditableModel } from './_directives/contenteditable-model';
import { AuthGuard } from './_guards/auth.guard';
import { AlertService, AuthenticationService, UserService, NavService } from './_services/index';
import { AlertComponent } from './_directives/alert.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { AuthInterceptor } from './_interceptors/authentication.interceptor';
import { ContentTypeInterceptor } from './_interceptors/contenttype.interceptor';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule,
    CKEditorModule,
  ],
  declarations: [
    AppComponent,
    NotesComponent,
    NoteEditorComponent,
    ContenteditableModel,
    AlertComponent,
    LoginComponent,
    RegisterComponent,
  ],
  providers: [
    NoteService,
    AlertService,
    AuthGuard,
    AuthenticationService,
    UserService,
    NavService,
    {
     provide: HTTP_INTERCEPTORS,
     useClass: AuthInterceptor,
     multi: true,
    },
    {
     provide: HTTP_INTERCEPTORS,
     useClass: ContentTypeInterceptor,
     multi: true,
    },
  ],
  bootstrap: [ AppComponent ],
})
export class AppModule { }

import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { NotesComponent }      from './notes.component';

const routes: Routes = [
  { path: '', component: NotesComponent },
  { path: 'notes',  component: NotesComponent },
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ],
})
export class AppRoutingModule {}

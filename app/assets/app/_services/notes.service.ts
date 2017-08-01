import { Injectable }    from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

import 'rxjs/add/operator/toPromise';

import { Note } from '../_models/note';

interface INotesResponse {
  notes: Note[];
}

@Injectable()
export class NoteService {

  private notesUrl = 'api/notes';

  constructor(private http: HttpClient) { }

  public getNotes(): Promise<Note[]> {
    return this.http
      .get<INotesResponse>(this.notesUrl)
      .toPromise()
      .then(response => response.notes as Note[])
      .catch(this.handleError);
  }

  public getNote(id: number): Promise<Note> {
    return this.http
      .get<Note>(`${this.notesUrl}/${id}`)
      .toPromise()
      .catch(this.handleError);
  }

  public update(note: Note): Promise<Note> {
    return this.http
      .put(this.notesUrl, note)
      .toPromise()
      .then(() => note)
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error); // for demo purposes only
    return Promise.reject(error.message || error);
  }
}

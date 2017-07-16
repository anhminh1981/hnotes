import { Injectable }    from '@angular/core';
import { Headers, Http } from '@angular/http';

import 'rxjs/add/operator/toPromise';

import { Note } from './note';

@Injectable()
export class NoteService {

  private headers = new Headers({'Content-Type': 'application/json'});
  private notesUrl = 'api/notes';

  constructor(private http: Http) { }

  public getNotes(): Promise<Note[]> {
    return this.http
      .get(this.notesUrl)
      .toPromise()
      .then(response => response.json().notes as Note[])
      .catch(this.handleError);
  }

  public getNote(id: number): Promise<Note> {
    return this.http
      .get(`${this.notesUrl}/${id}`)
      .toPromise()
      .then(response => response.json() as Note)
      .catch(this.handleError);
  }

  public update(note: Note): Promise<Note> {
    return this.http
      .post(this.notesUrl, JSON.stringify(note), {headers: this.headers})
      .toPromise()
      .then(() => note)
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error); // for demo purposes only
    return Promise.reject(error.message || error);
  }
}

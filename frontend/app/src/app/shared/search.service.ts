import { Injectable } from '@angular/core';
import { Http, RequestOptions, URLSearchParams } from '@angular/http';

import 'rxjs/add/operator/toPromise';

import { SearchResult } from './search-result';

@Injectable()
export class SearchService {
  private searchUrl = '/api/search';

  constructor(private http: Http) { }

  search(query: string): Promise<SearchResult[]> {
    let params = new URLSearchParams();
    params.set('url', query);

    let request_options = new RequestOptions({ params: params });

    function TransformResults(response): SearchResult[] {
      var results = [];

      var data = response.json();

      for (let i = 0; i < data.rows.length; i++)  {
        results.push(new SearchResult(data.rows[i][1], data.rows[i][0]));
      }

      return results;
    }

    return this.http.get(this.searchUrl, request_options)
      .toPromise()
      .then(TransformResults)
      .catch(this.handleError);
  }

  private handleError(error: any): Promise<any> {
    console.error('An error occurred', error);
    return Promise.reject(error.message || error);
  }
}

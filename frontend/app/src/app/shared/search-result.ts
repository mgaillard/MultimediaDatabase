export class SearchResult {
    distance: number;
    url: string;

    constructor(d: number, u: string) {
        this.distance = d;
        this.url = u;
    }
}

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';

@Component({
    selector: 'app-export-table',
    templateUrl: './export-table.component.html',
    styleUrls: ['./export-table.component.scss']
})
export class ExportTableComponent {

    fileName: string
    private _page = -1
    private pageSize = 1024 * 1024 * .5

    get page(): number {
        return this._page
    }

    set page(p: number) {
        if (this._page == p) return
        this._page = p

        this.http.get(environment.socketUrl + 'exports/' + this.fileName, {
            headers: {
                "Range": "bytes=0-10"
            },
            responseType: "text"
        }).subscribe(res => {
            console.log(res)
        })
    }

    constructor(
        private http: HttpClient,

        route: ActivatedRoute
    ) {
        this.fileName = route.snapshot.params["exportFile"]
        this.page = 0
    }

}

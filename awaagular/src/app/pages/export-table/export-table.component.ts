import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { transformTimestamp } from 'src/app/pipes/timestamp.pipe';
import { StationsService } from 'src/app/services/stations.service';

type Table = {
    timestamp: number
    rows: {
        stationId: number
        values: { [columnName: string]: string }
    }[]
    showColumns: string[]
}

type Column = {
    name: string
    readableValue: (str: string) => string
}

@Component({
    selector: 'app-export-table',
    templateUrl: './export-table.component.html',
    styleUrls: ['./export-table.component.scss']
})
export class ExportTableComponent {

    fileName: string
    private _page = -1
    private pageSize = 1024 * 5

    tables = [] as Table[]
    tableI = 0

    columns: { [tagname: string]: Column } = {
        "temp": {
            name: "Temperature",
            readableValue: str => str + "°C"
        },
        "wind": {
            name: "Wind speed",
            readableValue: str => str + " m/s"
        },
        "when": {
            name: "Time",
            readableValue: str => transformTimestamp(Number(str))
        }
    }

    get table(): Table {
        return this.tables[this.tableI]
    }

    get displayColumns(): string[] {
        return ["nr", "station", "country", ...this.table.showColumns]
    }

    get page(): number {
        return this._page
    }

    set page(p: number) {
        if (this._page == p) return
        this._page = p
        this.loadPage(p)
    }

    async loadPage(p: number) {

        let res = await this.http.get(environment.socketUrl + 'exports/' + this.fileName, {
            headers: {
                "Range": `bytes=${p * this.pageSize}-${(p + 1) * this.pageSize}`
            },
            responseType: "text"
        }).toPromise()

        let endTag = "</datepoint>"
        let lastEndTagI = res.lastIndexOf("</datepoint>")
        res = res.slice(0, lastEndTagI) + endTag + "</export>"

        console.log(res)

        let parser = new DOMParser()
        let xml = parser.parseFromString(res, "text/xml")
        let exportNode = xml.firstChild

        let datepoints = Array.from(exportNode.childNodes).filter(n => n.nodeName == "datepoint") as HTMLElement[]
        datepoints = datepoints.filter(dp => dp.getElementsByTagName("stations").length)
        let showColumns = []

        this.tables = datepoints.map(dp => ({
            timestamp: Number(dp.getAttribute("time")),
            rows: Array.from(dp.getElementsByTagName("stations").item(0).children).map(stNode => {
                let row = {
                    stationId: Number(stNode.getAttribute("id")),
                    values: {}
                }
                stNode.childNodes.forEach(c => {
                    let tagname = c.nodeName.toLowerCase()
                    if (!this.columns[tagname])
                        return // tagname is not recognized.
                    if (!showColumns.includes(tagname))
                        showColumns.push(tagname)

                    row.values[tagname] = c.textContent.trim()
                })
                return row
            }),
            showColumns
        }))
        console.log(this.tables)
    }

    constructor(
        private http: HttpClient,

        public stations: StationsService,

        route: ActivatedRoute
    ) {
        this.fileName = route.snapshot.params["exportFile"]
        this.page = 0
    }

}

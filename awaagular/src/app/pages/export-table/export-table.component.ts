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

const maxRequestBytes = 1024 * 512

@Component({
    selector: 'app-export-table',
    templateUrl: './export-table.component.html',
    styleUrls: ['./export-table.component.scss']
})
export class ExportTableComponent {

    fileName: string

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

    private bytesLoaded = [0, 0]
    private canLoadMore = true

    async loadPartOfXML(startByte: number, endByte: number): Promise<boolean> {

        let res = await this.http.get(environment.socketUrl + 'exports/' + this.fileName, {
            headers: {
                "Range": `bytes=${startByte}-${endByte}`
            },
            responseType: "text"
        }).toPromise()

        this.canLoadMore = res.indexOf("</export>") == -1

        let endTag = "</datepoint>"
        let lastEndTagI = res.lastIndexOf(endTag)
        if (lastEndTagI == -1) return false // no datepoints in response.

        res = res.slice(0, lastEndTagI)
        let resLength = res.length
        res += endTag + "</export>"
        res = "<export>" + res.slice(res.indexOf("<datepoint"))

        console.log(res, startByte, endByte)

        let parser = new DOMParser()
        let xml = parser.parseFromString(res, "text/xml")
        let exportNode = xml.firstChild

        let datepoints = Array.from(exportNode.childNodes).filter(n => n.nodeName == "datepoint") as HTMLElement[]
        datepoints = datepoints.filter(dp => dp.getElementsByTagName("stations").length)
        let showColumns = []

        let newTables = datepoints.map(dp => ({
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
        if (newTables.length) {
            this.tables = newTables
            this.bytesLoaded = [startByte, startByte + resLength]
        }

        console.log(newTables)
        return !!newTables.length
    }

    get canNavigateBack(): boolean {
        return this.tableI > 0 || this.bytesLoaded[0] > 0
    }

    get canNavigateForward(): boolean {
        return this.tableI < this.tables.length - 1 || this.canLoadMore
    }

    async navigate(forward: boolean) {
        if (!forward) {
            if (this.tableI > 0)
                this.tableI--

            else if (await this.loadPartOfXML(
                Math.max(0, this.bytesLoaded[0] - maxRequestBytes),
                this.bytesLoaded[0])
            )
                this.tableI = this.tables.length - 1

        } else {
            if (this.tableI < this.tables.length - 1)
                this.tableI++
            else if (await this.loadPartOfXML(
                this.bytesLoaded[1],
                this.bytesLoaded[1] + maxRequestBytes)
            )
                this.tableI = 0
        }
    }

    constructor(
        private http: HttpClient,

        public stations: StationsService,

        route: ActivatedRoute
    ) {
        this.fileName = route.snapshot.params["exportFile"]
        this.loadPartOfXML(0, maxRequestBytes)
    }

}

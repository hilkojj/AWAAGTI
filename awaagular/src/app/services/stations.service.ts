import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface Station {
    id: number
    name: string
    country: string
    lat: number
    lon: number
    elev: number
}

@Injectable({
    providedIn: 'root'
})
export class StationsService {

    array: Station[]

    constructor(
        private http: HttpClient
    ) {
        window["stations"] = this
        this.loadStations()
    }

    loadStations() {
        this.http.get<Station[]>("/assets/stations.json").subscribe(s => this.array = s)
    }

}

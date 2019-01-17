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

    array: Station[] = []
    byCountry: {[country: string]: Station[]} = {}

    get countries(): string[] {
        return Object.keys(this.byCountry)
    }

    constructor(
        private http: HttpClient
    ) {
        window["stations"] = this
        this.loadStations()
    }

    loadStations() {
        this.http.get<Station[]>("/assets/stations.json").subscribe(s => {
            this.array = s
            s.forEach(station => {
                !this.byCountry[station.country] && [this.byCountry[station.country] = []]
                this.byCountry[station.country].push(station)
            })
        })
    }

}

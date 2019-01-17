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

type countryCode = { name: string, code: string }

@Injectable({
    providedIn: 'root'
})
export class StationsService {

    array: Station[] = []
    byCountry: { [country: string]: Station[] } = {}

    countryCodes: countryCode[]

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
                if (!this.byCountry[station.country])
                    this.byCountry[station.country] = []

                this.byCountry[station.country].push(station)
            })
        })
        this.http.get<countryCode[]>("/assets/country-codes.json").subscribe(codes => this.countryCodes = codes)
    }

    countryCode(name: string) {
        name = name.toLowerCase()
        let code = this.countryCodes.find(
            c => c.name.toLowerCase().split(/ |-|,/).some(str => str.indexOf(name) != -1)
        )
        return code ? code.code : null
    }

}

import { Injectable } from '@angular/core';

export interface TimeFrame {
    from: number
    to: number
    interval: 3600 | 60 | 86400 | 2592000 | 1
}

type measurementType = "temperature"// | "windSpeed"

export interface Config {
    name: string
    stationIds: number[]
    timeFrame: TimeFrame
    what: measurementType[]
    sortBy?: measurementType
    limit?: number
    filter?: string
}

@Injectable({
    providedIn: 'root'
})
export class ConfigsService {

    constructor() { }

}

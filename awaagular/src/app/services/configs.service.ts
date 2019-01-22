import { Injectable } from '@angular/core';
import { SocketIoService } from './socket-io.service';

export interface TimeFrame {
    from: number
    to: number
    interval: 3600 | 60 | 86400 | 2592000 | 1
}

export type measurementType = "temperature" | "windSpeed"

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

    measurements = ["temperature", "windSpeed"]

    constructor(
        private io: SocketIoService
    ) {
    }

    exportConfig(config: Config) {
        if (!config.name)
            config.name = "untitled"
        this.io.socket.emit("export", config)
        this.io.socket.on("export error", err => {
            console.error(err)
            alert(err)
        })
    }

}

import { Injectable } from '@angular/core';
import { SocketIoService } from './socket-io.service';
import { AuthService } from './auth.service';

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
        private io: SocketIoService,
        private auth: AuthService
    ) {
    }

    get configs(): Config[] {
        return (this.auth.user.configs = this.auth.user.configs || [])
    }

    saveConfig(config: Config) {
        if (!config.name)
            config.name = prompt("Please enter a name for this export.") || "untitled"
        this.io.socket.emit("save config", config)
        this.configs.push(config)
    }

    exportConfig(config: Config) {
        if (!config.name)
            config.name = prompt("Please enter a name for this export.") || "untitled"
        this.io.socket.emit("export", config)
        this.io.socket.on("export error " + config.name, err => {
            console.error(err)
            alert(err)
            this.finishExport(config)
        })
    }

    finishExport(config: Config) {
        this.io.socket.off("export error " + config.name)
    }

}

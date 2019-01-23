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
    id: string | number
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

    get array(): Config[] {
        if (!this.auth.user) return null
        return (this.auth.user.configs = this.auth.user.configs || [])
    }

    saveConfig(config: Config) {
        if (!config.name)
            config.name = prompt("Please enter a name for this export.") || "untitled"
        this.io.socket.emit("save config", config)
        if (!this.array.includes(config))
            this.array.push(config)
    }

    deleteConfig(config: Config) {
        this.io.socket.emit("delete config", config)
        this.array.splice(this.array.indexOf(config), 1)
    }

    exportConfig(config: Config) {
        if (!config.name)
            config.name = prompt("Please enter a name for this export.") || "untitled"
        if (!config.id)
            config.id = (Math.random() * 10000) | 0
        this.io.socket.emit("export", config)
        this.io.socket.on("export error " + config.id, err => {
            console.error(err)
            alert(err)
            this.finishExport(config)
        })
    }

    finishExport(config: Config) {
        this.io.socket.off("export error " + config.id)
    }

}

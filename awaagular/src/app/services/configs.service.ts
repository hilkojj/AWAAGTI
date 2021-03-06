import { Injectable } from '@angular/core';
import { SocketIoService } from './socket-io.service';
import { AuthService } from './auth.service';
import { MatSnackBar } from '@angular/material';
import { environment } from 'src/environments/environment';

export interface TimeFrame {
    from: number
    to: number
    interval: 3600 | 60 | 86400 | 2592000 | 1
}

export type measurementType = "temperature" | "windSpeed"

export interface Export {
    config: Config
    progress: number
    downloadUrl?: string
    fileSize?: number
    error?: string
}

export interface Config {
    id: string | number
    name: string
    stationIds: number[]
    timeFrame: TimeFrame
    what: measurementType[]
    sortBy?: [measurementType, 'min' | 'max']
    limit?: number
    filter?: string
    filterThing?: string
    filterMode?: "between" | "greaterThan" | "smallerThan" | "equals" | "notEquals" | "equalsOrGreaterThan" | "equalsOrSmallerThan"
    filterValue?: number
    betweenLower?: number
    betweenUpper?: number
}

// stations=1234,1356;from=23423423;to=3453454353;interval=1;what=temperature,sfgfdgd;sortBy=32432432;limit=10;filter=temp,<,10\n

// file=ueouv5oehgjdn.xml\n

// progress=40\n

@Injectable({
    providedIn: 'root'
})
export class ConfigsService {

    measurements = ["temperature", "windSpeed"]
    filterModes = ["between", "greaterThan", "smallerThan", "equals", "notEquals", "equalsOrGreaterThan", "equalsOrSmallerThan"]
    exports = [] as Export[]

    constructor(
        private io: SocketIoService,
        private auth: AuthService,
        private snackbar: MatSnackBar
    ) {
    }

    get array(): Config[] {
        if (!this.auth.user) return null
        return (this.auth.user.configs = this.auth.user.configs || [])
    }

    saveConfig(config: Config) {
        this.completeConfig(config)
        this.io.socket.emit("save config", config)
        if (!this.array.includes(config))
            this.array.push(config)

        this.snackbar.open(config.name + " saved.", null, {
            duration: 2000
        })
    }

    deleteConfig(config: Config) {
        this.io.socket.emit("delete config", config)
        this.array.splice(this.array.indexOf(config), 1)

        this.snackbar.open(config.name + " deleted.", "UNDO", { duration: 5000 }).onAction().subscribe(() => {
            this.saveConfig(config)
        })
    }

    completeConfig(config: Config) {
        if (!config.name)
            config.name = prompt("Please enter a name for this export.") || "untitled"
        if (!config.id)
            config.id = (Math.random() * 10000) | 0
    }

    exportConfig(config: Config) {
        let exp = {
            config,
            progress: 0
        } as Export
        this.exports.push(exp)

        this.completeConfig(config)
        this.io.socket.emit("export", config)
        this.io.socket.on("export error " + config.id, err => {
            console.error(err)
            alert(err)
            exp.error = err
            this.finishExport(config)
        })
        this.io.socket.on("export warning " + config.id, warn => alert(warn))
        this.io.socket.on("export progress " + config.id, progress => exp.progress = Number(progress))
        this.io.socket.on("export done " + config.id, file => {
            exp.downloadUrl = environment.socketUrl + "exports/" + file
            console.log(exp.downloadUrl)
        })
        this.io.socket.on("export size " + config.id, size => {
            exp.fileSize = Number(size)
            console.log("export size", size)
        })
    }

    finishExport(config: Config) {
        this.io.socket.off("export error " + config.id)
        this.io.socket.off("export warning " + config.id)
        this.io.socket.off("export progress " + config.id)
        this.io.socket.off("export done " + config.id)
        this.io.socket.off("export size " + config.id)
    }

    closeExport(exp: Export) {
        this.finishExport(exp.config)
        this.exports.splice(this.exports.indexOf(exp), 1)
    }

}

import { Injectable } from '@angular/core';
import { AuthService } from './auth.service';
import * as io from 'socket.io-client'
import { environment } from 'src/environments/environment';
import { Config } from './configs.service';

@Injectable({
    providedIn: 'root'
})
export class SocketIoService {

    socket: SocketIOClient.Socket

    constructor(
        private auth: AuthService
    ) {
        auth.onTokenChange.push(() => this.init())
        auth.token && this.init()
    }

    private init() {
        if (this.socket) this.socket.close()

        if (!this.auth.token) return

        console.log("Creating socket")

        this.socket = (window as any).socket = io.connect(environment.socketUrl)
        this.socket.on("reconnect", () => this.emitJWT())
        this.emitJWT()
    }

    private emitJWT() {
        this.socket.emit("jwt", this.auth.token)
    }

}

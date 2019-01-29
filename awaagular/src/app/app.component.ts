import { Component } from '@angular/core';
import { StationsService } from './services/stations.service';
import { AuthService } from './services/auth.service';
import { SocketIoService } from './services/socket-io.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent {

    atTop = true

    private _links = [{
        label: "Home",
        path: "/",
        exact: true
    }, {
        label: "Export",
        path: "/export-data"
    }, {
        label: "Configurations",
        path: "/configs"
    }] 

    get links() {
        console.log("pizza")
        return this.auth.token ? this._links : this._links.slice(0, 1) // only show Home when not logged in.
    }

    constructor(
        private stations: StationsService, // to initialize stations
        private socketIO: SocketIoService,

        public auth: AuthService
    ) {
        setInterval(() => {
            this.atTop = window.scrollY < 100
        }, 100)
    }

}

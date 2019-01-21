import { Component } from '@angular/core';
import { StationsService } from './services/stations.service';
import { AuthService } from './services/auth.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent {

    atTop = true

    links = [{
        label: "Home",
        path: "/",
        exact: true
    }, {
        label: "Export",
        path: "/export-data"
    }];

    constructor(
        private stations: StationsService, // to initialize stations

        public auth: AuthService
    ) {
        setInterval(() => {
            this.atTop = window.scrollY < 100
        }, 100)
    }

}

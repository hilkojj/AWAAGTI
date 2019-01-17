import { Component } from '@angular/core';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss']
})
export class AppComponent {

    atTop = true

    constructor() {
        setInterval(() => {
            this.atTop = window.scrollY < 10
        }, 100)
    }

}

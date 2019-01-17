import { Component, OnInit } from '@angular/core';
import { StationsService } from 'src/app/services/stations.service';

@Component({
    selector: 'app-export',
    templateUrl: './export.component.html',
    styleUrls: ['./export.component.scss']
})
export class ExportComponent implements OnInit {

    constructor(
        private stations: StationsService
    ) { }

    ngOnInit() {
    }

}

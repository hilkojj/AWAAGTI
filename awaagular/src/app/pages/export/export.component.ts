import { Component, OnInit } from '@angular/core';
import { StationsService } from 'src/app/services/stations.service';

@Component({
    selector: 'app-export',
    templateUrl: './export.component.html',
    styleUrls: ['./export.component.scss']
})
export class ExportComponent implements OnInit {

    expanded: {[country: string]: boolean} = {}
    selectedStationIds: number[] = []

    constructor(
        public stations: StationsService
    ) { }

    ngOnInit() {
    }

    selectionChange(option) {
        if (!option.selected)
            this.selectedStationIds = this.selectedStationIds.filter(id => id != option.value)
        else this.selectedStationIds.push(option.value)
    }

}

import { Component, OnInit } from '@angular/core';
import { StationsService } from 'src/app/services/stations.service';

@Component({
    selector: 'app-export',
    templateUrl: './export.component.html',
    styleUrls: ['./export.component.scss']
})
export class ExportComponent implements OnInit {

    expanded: { [country: string]: boolean } = {}
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

    countrySelect(country: string, checkAll: boolean) {
        let stationIds = this.stations.byCountry[country].map(st => st.id)
        if (!checkAll)
            this.selectedStationIds = this.selectedStationIds.filter(id => !stationIds.includes(id))
        else
            stationIds
                .filter(id => !this.selectedStationIds.includes(id))
                .forEach(id => this.selectedStationIds.push(id))
    }

    // returns 2 if all selected, 1 if some, 0 if none
    countrySelected(country: string) {
        let stations = this.stations.byCountry[country]
        let selected = stations.filter(st => this.selectedStationIds.includes(st.id))
        if (selected.length == stations.length)
            return 2
        return selected.length ? 1 : 0
    }

}

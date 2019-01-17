import { Component, OnInit } from '@angular/core';
import { StationsService, Station } from 'src/app/services/stations.service';
import { Config } from 'src/app/services/configs.service';

@Component({
    selector: 'app-export',
    templateUrl: './export.component.html',
    styleUrls: ['./export.component.scss']
})
export class ExportComponent implements OnInit {

    expanded: { [country: string]: boolean } = {}

    searchInput = ""

    config: Config

    constructor(
        public stations: StationsService
    ) { }

    ngOnInit() {
        this.config = {
            name: "",
            stationIds: [],
            what: ["temperature"],
            timeFrame: {
                from: Date.now() - 24 * 60 * 60 * 1000,
                to: -1,
                interval: 3600
            }
        }
    }

    selectionChange(option) {
        if (!option.selected)
            this.config.stationIds = this.config.stationIds.filter(id => id != option.value)
        else this.config.stationIds.push(option.value)
    }

    countrySelect(country: string, checkAll: boolean) {
        let stationIds = this.stationsByCountry(country).map(st => st.id)
        if (!checkAll)
            this.config.stationIds = this.config.stationIds.filter(id => !stationIds.includes(id))
        else
            stationIds
                .filter(id => !this.config.stationIds.includes(id))
                .forEach(id => this.config.stationIds.push(id))
    }

    // returns 2 if all selected, 1 if some, 0 if none
    countrySelected(country: string) {
        let stations = this.stations.byCountry[country]
        let selected = stations.filter(st => this.config.stationIds.includes(st.id))
        if (selected.length == stations.length)
            return 2
        return selected.length ? 1 : 0
    }

    matchesFilter(name: string): boolean {
        name = name.toLowerCase()
        return this.searchInput.toLowerCase().split(" ").some(i => name.indexOf(i) != -1)
    }

    get countries(): string[] {
        return this.stations.countries.filter(c => this.matchesFilter(c) || this.stationsByCountry(c).length).sort()
    }

    stationsByCountry(country: string): Station[] {
        return this.stations.byCountry[country]
            .filter(st => this.matchesFilter(st.name) || this.matchesFilter(st.country))
            .sort((a, b) => a.name < b.name ? -1 : 1)
    }

}

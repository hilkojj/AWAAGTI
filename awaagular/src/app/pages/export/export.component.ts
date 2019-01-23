import { Component, OnInit, DoCheck } from '@angular/core';
import { StationsService, Station } from 'src/app/services/stations.service';
import { Config, ConfigsService, measurementType } from 'src/app/services/configs.service';

declare var L: any

@Component({
    selector: 'app-export',
    templateUrl: './export.component.html',
    styleUrls: ['./export.component.scss']
})
export class ExportComponent implements OnInit, DoCheck {

    expanded: { [country: string]: boolean } = {}

    searchInput = ""

    config: Config

    map

    measurementSelect(selected: boolean, name: measurementType) {
        if (selected && !this.config.what.includes(name))
            this.config.what.push(name)
        else this.config.what = this.config.what.filter(n => n != name)
    }

    constructor(
        public stations: StationsService,
        public configs: ConfigsService
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
        window["config"] = this.config
        this.fromDate = new Date(this.config.timeFrame.from)
        this.toDate = this.config.timeFrame.to == -1 ? new Date() : new Date(this.config.timeFrame.to);
        [this.fromDate, this.toDate].forEach((d, i) => {
            this[i == 0 ? 'fromMinutes' : 'toMinutes'] = d.getHours() * 60 + d.getMinutes()
            d.setHours(0)
            d.setMinutes(0)
            d.setSeconds(0)
        })
        this.dateChanged()

        this.map = new L.Map("map").setView([40, 70], 2)

        L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=sk.eyJ1IjoibG9zb3MiLCJhIjoiY2pueXhwZ3RvMDFzdzNrbXFicnlmY3Q1YiJ9.ixxDbuWyXhWX4PY4qt07ZA', {
            attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
            maxZoom: 18,
            id: 'mapbox.streets',
            accessToken: 'your.mapbox.access.token'
        }).addTo(this.map)
    }

    markersAdded = false

    ngDoCheck() {
        if (this.markersAdded || this.stations.array.length == 0)
            return

        let icon = new L.DivIcon({
            className: 'station-icon',
            html: 'bla'
        })

        var markerClusters = L.markerClusterGroup({
            maxClusterRadius: 80
        })

        this.stations.array.forEach(st => {
            try {
                markerClusters.addLayer(
                    L.marker([st.lat, st.lon], { icon: icon })
                )
            } catch (e) { }
        })
        this.map.addLayer(markerClusters)
        this.markersAdded = true
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
        return this.stations.countries.filter(c => this.matchesFilter(c)).sort()
    }

    get countriesWithStations(): string[] {
        return this.stations.countries.filter(c => this.matchesFilter(c) || this.stationsByCountry(c).length).sort()
    }

    stationsByCountry(country: string): Station[] {
        return this.stations.byCountry[country]
            .filter(st => this.matchesFilter(st.name) || this.matchesFilter(st.country))
            .sort((a, b) => a.name < b.name ? -1 : 1)
    }

    toDate: Date
    toMinutes: number
    fromDate: Date
    fromMinutes: number

    get toTime(): string {
        return this.minutesToTimeStr(this.toMinutes)
    }

    set toTime(str: string) {
        this.config.timeFrame.to = this.toDate.getTime() + this.timeStrToMinutes(str) * 60000
    }

    get fromTime(): string {
        return this.minutesToTimeStr(this.fromMinutes)
    }

    set fromTime(str: string) {
        this.config.timeFrame.from = this.fromDate.getTime() + this.timeStrToMinutes(str) * 60000
    }

    timeStrToMinutes(input: string) {
        input = input.replace(".", ":")
        let hhmm = input.split(":")
        if (hhmm.length < 2)
            return 0
        let hours = Number(hhmm[0])
        let minutes = Number(hhmm[1])
        return (hours * 60) + minutes
    }

    minutesToTimeStr(input: number) {
        let minutes = input % 60
        return (input / 60 | 0) + ":" + (minutes < 10 ? "0" : "") + minutes
    }

    setToTime(current: boolean) {
        if (current)
            this.config.timeFrame.to = -1
        else this.dateChanged()
    }

    dateChanged() {
        this.config.timeFrame.from = this.fromDate.getTime() + this.fromMinutes * 60000
        if (this.config.timeFrame.to > -1)
            this.config.timeFrame.to = this.toDate.getTime() + this.toMinutes * 60000
    }

}

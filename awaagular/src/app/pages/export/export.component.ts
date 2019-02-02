import { Component, OnInit, DoCheck } from '@angular/core';
import { StationsService, Station } from 'src/app/services/stations.service';
import { Config, ConfigsService, measurementType } from 'src/app/services/configs.service';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

declare var L: any

@Component({
    selector: 'app-export',
    templateUrl: './export.component.html',
    styleUrls: ['./export.component.scss']
})
export class ExportComponent implements OnInit, DoCheck {

    expanded: { [country: string]: boolean } = {}

    searchInput: string = ""

    config: Config

    map

    measurementSelect(selected: boolean, name: measurementType) {
        if (selected && !this.config.what.includes(name))
            this.config.what.push(name)
        else this.config.what = this.config.what.filter(n => n != name)
    }

    constructor(
        public stations: StationsService,
        public configs: ConfigsService,
        route: ActivatedRoute,
        router: Router,
        auth: AuthService
    ) {
        if (!auth.token) {
            router.navigateByUrl("/")
            return
        }
        if (route.snapshot.params["configIndex"] && configs.array)
            this.config = configs.array[route.snapshot.params["configIndex"]]
    }

    ngOnInit() {
        this.config = this.config || {
            id: null,
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

        this.map = new L.Map("map").setView([30, 30], 2)
        window["map"] = this.map

        L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=sk.eyJ1IjoibG9zb3MiLCJhIjoiY2pueXhwZ3RvMDFzdzNrbXFicnlmY3Q1YiJ9.ixxDbuWyXhWX4PY4qt07ZA', {
            attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
            maxZoom: 18,
            id: 'mapbox.streets',
            accessToken: 'your.mapbox.access.token'
        }).addTo(this.map)
        setInterval(() => this.map.invalidateSize(), 500)
        setTimeout(() => this.map.invalidateSize(), 100)
    }

    markersAdded = false
    markers = {} as { [stationId: number]: any }

    ngDoCheck() {
        if (this.markersAdded || this.stations.array.length == 0)
            return

        this.prevCountries = null
        this.prevCountriesWSt = null

        var markerClusters = L.markerClusterGroup({
            maxClusterRadius: 65
        })

        let clickCB = e => {
            console.log(e)
            this.selectionChange({
                selected: !this.config.stationIds.includes(e.target.options.station.id),
                value: e.target.options.station.id
            })
        }
        this.stations.array.forEach(st => {
            try {
                let marker = this.markers[st.id] = L.marker([st.lat, st.lon], { icon: this.markerIcon(st), station: st })
                markerClusters.addLayer(marker)
                marker.on("click", clickCB)
            } catch (e) { }
        })
        this.map.addLayer(markerClusters)
        this.markersAdded = true

        markerClusters.on('clusterclick', (a) => {
            let markers = a.layer.getAllChildMarkers()
            let countries = new Set()
            markers.forEach(m => countries.add(m.options.station.country))

            if (countries.size > 5)
                return
            this.searchInput = ""
            countries.forEach(c => c && (this.searchInput += c.toLowerCase() + " "))
        })

        if (this.config.stationIds.length)
            this.focusOnStations(this.stations.array.filter(st => this.config.stationIds.includes(st.id)))
    }

    private markerIcon(station: Station) {
        let selected = this.config.stationIds.includes(station.id)
        return new L.DivIcon({
            className: 'station-icon mat-elevation-z1' + (selected ? " selected" : ""),
            html: `
                <mat-icon class="mat-icon material-icons" role="img" aria-hidden="true">${selected ? 'done' : 'add'}</mat-icon>
            ` + station.name.toLowerCase()
        })
    }

    selectionChange(option) {
        if (!option.selected)
            this.config.stationIds = this.config.stationIds.filter(id => id != option.value)
        else this.config.stationIds.push(option.value)

        this.updateMarkerIcon(option.value)
    }

    countrySelect(country: string, checkAll: boolean) {
        let stationIds = this.stationsByCountry(country).map(st => st.id)
        if (!checkAll)
            this.config.stationIds = this.config.stationIds.filter(id => !stationIds.includes(id))
        else stationIds
            .filter(id => !this.config.stationIds.includes(id))
            .forEach(id => this.config.stationIds.push(id))

        stationIds.forEach(id => this.updateMarkerIcon(id))
    }

    updateMarkerIcon(stationId: number) {
        let marker = this.markers[stationId]
        if (marker)
            marker.setIcon(this.markerIcon(marker.options.station))
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
        if (!this.searchInput.length) return true
        name = name.toLowerCase()
        return this.searchInput.toLowerCase().trim().split(" ").some(i => name.indexOf(i) != -1)
    }

    private prevCountries: string[]
    private prevCountriesFilter = ""
    get countries(): string[] {
        if (this.prevCountries && this.prevCountriesFilter == this.searchInput)
            return this.prevCountries
        this.prevCountriesFilter = this.searchInput
        return this.prevCountries = this.stations.countries.filter(c => this.matchesFilter(c)).sort()
    }

    private prevCountriesWSt: string[]
    private prevCountriesWStFilter = ""
    get countriesWithStations(): string[] {
        if (this.prevCountriesWSt && this.prevCountriesWStFilter == this.searchInput)
            return this.prevCountriesWSt
        this.prevCountriesWStFilter = this.searchInput
        return this.prevCountriesWSt = this.stations.countries.filter(c => this.matchesFilter(c) || this.stationsByCountry(c).length).sort()
    }

    stationsByCountry(country: string): Station[] {
        return this.stations.byCountry[country]
            .filter(st => this.matchesFilter(st.name) || this.matchesFilter(st.country))
            .sort((a, b) => a.name < b.name ? -1 : 1)
    }

    private lastFocusedOn
    focusOnCountry(country: string) {
        if (this.lastFocusedOn == country) return
        this.focusOnStations(this.stations.byCountry[country])
        this.lastFocusedOn = country
    }
    
    focusOnStations(stations: Station[]) {
        let minLat: number, maxLat: number
        let minLon: number, maxLon: number
        stations.forEach(st => {
            if (st.lat < minLat || !minLat)
                minLat = st.lat
            if (st.lat > maxLat || !maxLat)
                maxLat = st.lat
            if (st.lon < minLon || !minLon)
                minLon = st.lon
            if (st.lon > maxLon || !maxLon)
                maxLon = st.lon

            this.map.fitBounds([
                [minLat - 1, minLon - 1],
                [maxLat + 1, maxLon + 1]
            ])
            if (this.map.getZoom() > 10)
                this.map.setZoom(10)
        })
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
        else {
            this.config.timeFrame.to = 0
            this.dateChanged()
        }
    }

    dateChanged() {
        this.config.timeFrame.from = this.fromDate.getTime() + this.fromMinutes * 60000
        if (this.config.timeFrame.to > -1)
            this.config.timeFrame.to = this.toDate.getTime() + this.toMinutes * 60000
    }

}

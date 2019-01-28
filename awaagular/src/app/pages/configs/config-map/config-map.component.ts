import { Component, OnInit, Input } from '@angular/core';
import { Config } from 'src/app/services/configs.service';
import { StationsService } from 'src/app/services/stations.service';

declare var L: any

@Component({
    selector: 'app-config-map',
    templateUrl: './config-map.component.html',
    styleUrls: ['./config-map.component.scss']
})
export class ConfigMapComponent implements OnInit {

    @Input()
    config: Config

    map

    constructor(
        private stations: StationsService
    ) { }

    ngOnInit() {
        setTimeout(() => {
            if (!this.stations.array.length)
                return this.ngOnInit()
            this.init()
        }) // otherwise map div is not found
    }

    init() {
        this.map = new L.Map("map-" + this.config.id, {
            attributionControl: false,
            zoomControl: false,
            boxZoom: false,
            doubleClickZoom: false,
            dragging: false
        }).setView([30, 30], 2)

        let icon = new L.DivIcon({
            className: 'marker'
        })

        L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=sk.eyJ1IjoibG9zb3MiLCJhIjoiY2pueXhwZ3RvMDFzdzNrbXFicnlmY3Q1YiJ9.ixxDbuWyXhWX4PY4qt07ZA', {
            attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
            maxZoom: 18,
            id: 'mapbox.streets',
            accessToken: 'your.mapbox.access.token'
        }).addTo(this.map)
        setInterval(() => this.map.invalidateSize(), 500)
        setTimeout(() => this.map.invalidateSize(), 100)

        var markerClusters = L.markerClusterGroup({
            maxClusterRadius: 35
        })

        let minLat: number, maxLat: number
        let minLon: number, maxLon: number
        this.config.stationIds.forEach(id => {
            let st = this.stations.array.find(st => st.id == id)
            if (st.lat < minLat || !minLat)
                minLat = st.lat
            if (st.lat > maxLat || !maxLat)
                maxLat = st.lat
            if (st.lon < minLon || !minLon)
                minLon = st.lon
            if (st.lon > maxLon || !maxLon)
                maxLon = st.lon
            try {
                markerClusters.addLayer(L.marker([st.lat, st.lon], { icon }))
            } catch (e) { }
        })
        setTimeout(() => {
            this.map.fitBounds([
                [minLat - .15, minLon - .15],
                [maxLat + .15, maxLon + .15]
            ])
        }, 500)
        
        this.map.addLayer(markerClusters)
    }

}

import { Component, OnInit } from '@angular/core';
import { ConfigsService } from 'src/app/services/configs.service';

@Component({
    selector: 'app-exports',
    templateUrl: './exports.component.html',
    styleUrls: ['./exports.component.scss']
})
export class ExportsComponent implements OnInit {

    constructor(
        public configs: ConfigsService
    ) { }

    ngOnInit() {
    }

}

import { Component, OnInit } from '@angular/core';
import { ConfigsService } from 'src/app/services/configs.service';

@Component({
    selector: 'app-configs',
    templateUrl: './configs.component.html',
    styleUrls: ['./configs.component.scss']
})
export class ConfigsComponent implements OnInit {

    constructor(
        public configs: ConfigsService
    ) { }

    ngOnInit() {
    }

}

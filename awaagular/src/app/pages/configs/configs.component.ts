import { Component, OnInit } from '@angular/core';
import { ConfigsService } from 'src/app/services/configs.service';
import { AuthService } from 'src/app/services/auth.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-configs',
    templateUrl: './configs.component.html',
    styleUrls: ['./configs.component.scss']
})
export class ConfigsComponent implements OnInit {

    constructor(
        public configs: ConfigsService,
        auth: AuthService,
        router: Router
    ) {
        if (!auth.token) {
            router.navigateByUrl("/")
            return
        }
    }

    ngOnInit() {
    }

}

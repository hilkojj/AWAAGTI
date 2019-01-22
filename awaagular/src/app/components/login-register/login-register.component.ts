import { Component, OnInit } from '@angular/core';
import { AuthService } from 'src/app/services/auth.service';
import { MatDialogRef } from '@angular/material';

@Component({
    selector: 'app-login-register',
    templateUrl: './login-register.component.html',
    styleUrls: ['./login-register.component.scss']
})
export class LoginRegisterComponent {

    username: string
    password: string

    constructor(
        private dialogRef: MatDialogRef<LoginRegisterComponent>
    ) { }

    done(register?: boolean) {
        this.dialogRef.close({
            username: this.username, password: this.password, register
        })
    }

}

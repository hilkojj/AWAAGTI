import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FlexLayoutModule } from '@angular/flex-layout';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MaterialModule } from './material/material.module';
import { HomeComponent } from './pages/home/home.component';
import { ExportComponent } from './pages/export/export.component';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { MatNativeDateModule } from '@angular/material';
import { DecamelcasePipe } from './pipes/decamelcase.pipe';
import { LoginRegisterComponent } from './components/login-register/login-register.component';
import { TimestampPipe } from './pipes/timestamp.pipe';
import { ConfigsComponent } from './pages/configs/configs.component';
import { ConfigMapComponent } from './pages/configs/config-map/config-map.component';
import { ExportsComponent } from './components/exports/exports.component';

@NgModule({
    declarations: [
        AppComponent,
        HomeComponent,
        ExportComponent,
        DecamelcasePipe,
        TimestampPipe,
        LoginRegisterComponent,
        ConfigsComponent,
        ConfigMapComponent,
        ExportsComponent
    ],
    imports: [
        FormsModule,
        HttpClientModule,
        FlexLayoutModule,
        BrowserModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        MaterialModule,
        MatNativeDateModule
    ],
    providers: [
        MatNativeDateModule
    ],
    bootstrap: [AppComponent],
    entryComponents: [
        LoginRegisterComponent
    ]
})
export class AppModule { }

import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { ExportComponent } from './pages/export/export.component';
import { ConfigsComponent } from './pages/configs/configs.component';
import { ExportTableComponent } from './pages/export-table/export-table.component';

const routes: Routes = [
    {
        path: "",
        component: HomeComponent
    },
    {
        path: "export-data",
        component: ExportComponent
    },
    {
        path: "export-data/:configIndex",
        component: ExportComponent
    },
    {
        path: "configs",
        component: ConfigsComponent
    },
    {
        path: "show-in-table/:exportFile",
        component: ExportTableComponent
    }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }

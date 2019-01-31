import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatCardModule, MatToolbarModule, MatTabsModule,  MatListModule, MatButtonModule,  MatIconModule, MatCheckboxModule, MatInputModule, MatAutocompleteModule, MatDatepickerModule, MatSelectModule, MatDialogModule, MatSnackBarModule, MatProgressSpinnerModule } from '@angular/material';

const components = [
    MatCardModule,
    MatToolbarModule,
    MatTabsModule,
    MatListModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatInputModule,
    MatAutocompleteModule,
    MatDatepickerModule,
    MatSelectModule,
    MatDialogModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
]

@NgModule({
    declarations: [],
    imports: [
        CommonModule,
        ...components
    ],
    exports: components
})
export class MaterialModule { }

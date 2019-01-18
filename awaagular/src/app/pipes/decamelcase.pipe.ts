import { Pipe, PipeTransform } from '@angular/core';
import * as decamelize from "decamelize"

@Pipe({
    name: 'decamelcase'
})
export class DecamelcasePipe implements PipeTransform {

    transform(value: string): string {
        if (!value) return ""
        return decamelize(value, " ")
    }

}

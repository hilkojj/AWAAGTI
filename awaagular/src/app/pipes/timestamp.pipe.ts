import { Pipe, PipeTransform } from '@angular/core';

export const months = [
    "January", "February", "March", "April", "May", 
    "June", "July", "August", "September", "October", "November", "December"
]

@Pipe({
    name: 'timestamp'
})
export class TimestampPipe implements PipeTransform {

    transform(timestamp: number, option?: string): string {

        var a = new Date(timestamp);
        var b = new Date();

        var year = a.getFullYear();
        var month = a.getMonth();
        var date = a.getDate();
        var hour = a.getHours();
        var min: any = a.getMinutes();
        var sec: any = a.getSeconds();
        var string = '';
        var minStr = min < 10 ? "0" + min : min;
        var secStr = sec < 10 ? "0" + sec : sec;

        if (option == "onlyTime") return hour + ":" + minStr;

        if (option == "short") {

            var thisYear = year == b.getFullYear();
            var thisMonth = a.getMonth() == b.getMonth();

            if (thisYear && thisMonth) {

                if (date == b.getDate() -1) return "Yesterday";
                if (date == b.getDate()) return hour + ":" + minStr;
            }

            return `${date}-${month + 1}-${year}`;
        }
        
        if (year == b.getFullYear()) {
            if (date != b.getDate() || month != b.getMonth()) {
                if (month == b.getMonth() && b.getDate() - date == 1) {
                    string += 'Yesterday ';
                } else {
                    string += date + ' ' + months[month] + ' ';
                }
            }
        } else {
            string += date + ' ' + months[month] + ' ' + year + ' ';
        }
        
        string += 'at ' + hour + ':' + minStr;
        if (option == "seconds") string += ":" + secStr
        return string;

    }

}

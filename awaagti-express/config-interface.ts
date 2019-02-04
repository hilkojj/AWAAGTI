export interface Config {
    id: string | number
    name: string
    stationIds: number[]
    timeFrame: TimeFrame
    what: measurementType[]
    sortBy?: [measurementType, 'min' | 'max']
    limit?: number
    filter?: string
    filterThing?: measurementType
    filterMode?: "between" | "greaterThan" | "smallerThan" | "equals" | "notEquals" | "equalsOrGreaterThan" | "equalsOrSmallerThan"
    filterValue?: number
    betweenLower?: number
    betweenUpper?: number
}

export interface TimeFrame {
    from: number
    to: number
    interval: 3600 | 60 | 86400 | 2592000 | 1
}

export type measurementType = "temperature" | "windSpeed"

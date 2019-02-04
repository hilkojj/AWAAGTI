/**
 * This file is responsible for:
 *  - sending Queries made by the user to the Database
 *  - sending real time Query progess to the user
 */

const net = require('net')
const fs = require('fs')
const promisify = require("util").promisify
const exportsFolder = require("./index").exportsFolder

// example query that can be sent to the database:
// stations=1234,1356;from=23423423;to=3453454353;interval=1;what=temp,sfgfdgd;sortBy=32432432;limit=10;filter=temp,<,10\n

/**
 * This function converts a Configuration to a Query.
 * Configurations are made by the user in the Webapp
 * The query can be sent to the Database.
 * 
 * A Configuration is basically a JSON object containing everything that is needed to construct a Query.
 * 
 * open "./config.interface.ts" to see what is stored in a Configuration
 * 
 * @param {Config} config
 * @returns {string} A Query that can be sent to the database.
 */
const configToQuery = config => {
    let q = `from=${config.timeFrame.from};to=${config.timeFrame.to};`
        + `interval=${config.timeFrame.interval};what=${config.what.map(t => t.slice(0, 4)).join(",")};`

    if (typeof config.stationIds == "object" && config.stationIds.length)
        q += `stations=${config.stationIds.join(",")};`

    if (typeof config.sortBy == "object")
        q += `sortBy=${config.sortBy[0].slice(0, 4)}_${config.sortBy[1] == 'min' ? 'min' : 'max'};`

    if (config.limit)
        q += `limit=${config.limit};`

    if (config.filterThing && config.filterMode) {

        let filterThing = String(config.filterThing).slice(0, 4)

        switch (config.filterMode) {
            case "between":
                q += `filter=${filterThing},between,${config.betweenLower},${config.betweenUpper};`
                break
            default:
                let operand = { "greaterThan": ">", "smallerThan": "<", "equals": "==", "notEquals": "!=", "equalsOrGreaterThan": ">=", "equalsOrSmallerThan": "<=" }[config.filterMode]
                if (operand)
                    q += `filter=${filterThing},${operand},${config.filterValue};`
        }
    }

    return q + "\r\n"
}

/**
 * This function will try to send a query to the Database.
 * This function also takes several Callbacks that are used to report errors, warnings, progress and completion
 * 
 * @param {Config}                      config          see "./config.interface.ts"
 * @param {(progress: number) => void}  onProgress 
 * @param {(fileName: string, fileSize: number) => void} onDone 
 * @param {(warning: string) => void}   onWarning 
 * @param {(error: string) => void}     onError 
 */
const exportConfig = (config, onProgress, onDone, onWarning, onError) => {

    let client = new net.Socket()
    let file = null

    // this interval will run each second to see if the export is saved and can be sent to the user.
    let interval = setInterval(async () => {
        if (!file) return
        let path = exportsFolder + file

        if (await promisify(fs.exists)(path)) { // check if file exists.

            console.log(path, "DOES EXIST !! !!!! !! WOWIE")
            onDone(file, (await promisify(fs.stat)(path)).size)
            clearInterval(interval) // stop checking because file exists.
        }
    }, 1000)

    /**
     * This function will handle data that is sent by the database.
     * This data is NOT the weather data, but it is information about query progress, errors, etc.
     * 
     * @param {string} data from the database
     */
    let handleData = data => {
        data = data.trim()
        console.log(data)
        if (data.startsWith("file=")) {
            file = data.split("file=")[1].trim()
            console.log("wowie we have a filename:", file)
        }

        if (data.startsWith("error="))
            onError(data.split("error=")[1])

        if (data.startsWith("warning="))
            onWarning(data.split("warning=")[1])

        if (data.startsWith("progress="))
            onProgress(Number(data.split("progress=")[1]))
    }

    client.on("data", data => data.toString().split("\n").forEach(handleData))

    // connect to database
    client.connect(12345, '127.0.0.1', () => {
        console.log('Connected')
        // send query:
        let q = configToQuery(config)
        console.log(q)
        client.write(q)
    })
    client.on("error", err => {
        // connection to database failed.
        console.error(err)
        onError("Timos server is broken. Blame timo")
    })
    client.on("end", () => {
        console.log("connection ended")
        if (!file) clearInterval(interval)
        client.end()
    })
    client.on('timeout', () => console.log('Client connection timeout.'))

    return {
        // function to stop the query before its finished.
        end: () => {
            client.end()
            clearInterval(interval)
        }
    }
}

module.exports = exportConfig

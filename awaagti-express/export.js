const net = require('net')
const fs = require('fs')
const promisify = require("util").promisify
const exportsFolder = require("./index").exportsFolder

//stations=1234,1356;from=23423423;to=3453454353;interval=1;what=temp,sfgfdgd;sortBy=32432432;limit=10;filter=temp,<,10\n

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

module.exports = (config, onProgress, onDone, onWarning, onError) => {

    let client = new net.Socket()
    let file = null

    let interval = setInterval(async () => {
        if (!file) return
        let path = exportsFolder + file

        if (await promisify(fs.exists)(path)) {
            console.log(path, "DOES EXIST !! !!!! !! WOWIE")
            onDone(file, (await promisify(fs.stat)(path)).size)
            clearInterval(interval)
        }

    }, 1000)

    let handleData = data => {
        console.log("hoi")
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

    client.connect(12345, '127.0.0.1', () => {
        console.log('Connected')
        let q = configToQuery(config)
        console.log(q)
        client.write(q)
    })
    client.on("error", err => {
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
        end: () => {
            client.end()
            clearInterval(interval)
        }
    }
}

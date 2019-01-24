const net = require('net')
const fs = require('fs')

//stations=1234,1356;from=23423423;to=3453454353;interval=1;what=temp,sfgfdgd;sortBy=32432432;limit=10;filter=temp,<,10\n

const configToQuery = config => {
    let q = `stations=${config.stationIds.join(",")};from=${config.timeFrame.from};to=${config.timeFrame.to};`
        + `interval=${config.timeFrame.interval};what=${config.what.join(",")};`

    if (config.sortBy)
        q += `sortBy=${config.sortBy};`
    if (config.limit)
        q += `limit=${config.limit};`
    return q + "\r\n"
}

module.exports = (config, onProgress, onDone, onError) => {

    let client = new net.Socket()
    let file = null

    let interval = setInterval(() => {
        if (!file) return
        fs.exists(file, exists => {
            if (!exists) return
            onDone(file)
            clearInterval(interval)
        })
    }, 1000)

    client.on("data", data => {
        console.log("hoi")
        console.log(data.toString())
        if (data.startsWith("file=")) {
            file = data.split("file=")[1]
            console.log("wowie we have a filename:", file)
        }

        if (data.startsWith("error="))
            onError(data.split("error=")[1])

        if (data.startsWith("progress="))
            onProgress(Number(data.split("progress=")[1]))
    })
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
        client.end()
    })
    client.on('timeout', function () {
        console.log('Client connection timeout.');
    })

    return {
        end: () => {
            client.end()
            clearInterval(interval)
        }
    }
}

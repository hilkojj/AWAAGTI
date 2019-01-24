const net = require('net')

//stations=1234,1356;from=23423423;to=3453454353;interval=1;what=temp,sfgfdgd;sortBy=32432432;limit=10;filter=temp,<,10\n

const configToQuery = config => {
    let q = `stations=${config.stationIds.join(",")};from=${config.timeFrame.from};to=${config.timeFrame.to};`
        + `interval=${config.timeFrame.interval};what=${config.what.join(",")};`

    if (config.sortBy)
        q += `sortBy=${config.sortBy};`
    if (config.limit)
        q += `limit=${config.limit};`
    return q
}

module.exports = (config, onProgress, onDone, onError) => {

    let client = new net.Socket()
    client.connect(12345, '127.0.0.1', () => {
        console.log('Connected')
        client.write(configToQuery(config))
    })
    client.on("error", err => {
        console.error(err)
        onError("Timos server is not running. Blame timo")
    })

}

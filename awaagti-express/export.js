const net = require('net')

module.exports = (config, onProgress, onDone, onError) => {

    let client = new net.Socket()
    client.connect(12345, '127.0.0.1', () => {
        console.log('Connected')
        client.write(`kut timo`)
    })
    client.on("error", err => {
        console.error(err)
        onError("Timos server is not running. Blame timo")
    })

}

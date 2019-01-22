const jwt = require('jsonwebtoken')
const auth = require("./auth")
const exportConfig = require("./export")

module.exports = io => {

    io.on('connection', clientSocket => {
        console.log("hi someone connected")
        clientSocket.on('jwt', token => {
            let payload = jwt.verify(token, auth.secret)
            let user = auth.users[payload.username]

            if (user) initClient(clientSocket, user)
            else clientSocket.emit("invalid token")
        })
        clientSocket.on('disconnect', () => console.log("k bye"))
    })

}

const initClient = (socket, user) => {
    console.log("hi", user.username)

    socket.on("export", config => {

        config.name = String(config.name || "")

        if (!config.name.length)
            return

        exportConfig(
            config,
            progress => {
                console.log("Export progress " + config.name, progress)
                socket.emit("export progress " + config.name, progress)
            },
            file => {
                console.log("Export can be found in", file)
                socket.emit("export done" + config.name, file)
            },
            err => {
                console.log("Export error " + config.name)
                socket.emit("export error " + config.name, err)
            }
        )
    })
}
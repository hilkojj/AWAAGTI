/**
 * This file is responsible for 
 * setting up WebSockets with the users and
 * registering event listeners.
 * 
 * WebSockets need to be authenticated by JSON Web Token which are created when a user signs in using "/api/login". 
 * 
 * The code in all these event listeners basically
 * makes it possible for the user to call the functions in "./export.js"
 */

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
    let exports = []

    socket.on("export", config => {

        config.name = String(config.name || "")

        if (!config.name.length)
            return

        let exp = exportConfig(
            config,
            progress => {
                console.log("Export progress " + config.id, progress)
                socket.emit("export progress " + config.id, progress)
            },
            (file, size) => {
                console.log("Export can be found in", file)
                console.log("Export size:", size)
                socket.emit("export done " + config.id, file)
                socket.emit("export size " + config.id, size)
            },
            warn => {
                console.log("Export warning " + config.id)
                socket.emit("export warning " + config.id, warn)
            },
            err => {
                console.log("Export error " + config.id)
                socket.emit("export error " + config.id, err)
            }
        )
        exports.push(exp)
    })

    socket.on("save config", config => {
        let configs = (user.configs = user.configs || [])
        let existingI = configs.findIndex(c => c.id == config.id)
        if (existingI != -1)
            configs[existingI] = config
        else
            configs.push(config)
        auth.saveUsers()
    })

    socket.on("delete config", config => {
        if (!user.configs) return
        let i = user.configs.findIndex(c => c.id == config.id)
        if (i != -1)
            user.configs.splice(i, 1)
        auth.saveUsers()
    })

    socket.on("disconnect", () => {
        if (!exports.length) return
        console.log("Ending", exports.length, "exports")
        exports.forEach(e => e.end())
    })

}
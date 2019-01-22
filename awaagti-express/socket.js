const jwt = require('jsonwebtoken')
const auth = require("./auth")

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
        
    })
}
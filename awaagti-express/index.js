console.log(`
▄▄▄· ▄▄▌ ▐ ▄▌ ▄▄▄·  ▄▄▄·  ▄▄ • ▄▄▄▄▄▪  
▐█ ▀█ ██· █▌▐█▐█ ▀█ ▐█ ▀█ ▐█ ▀ ▪•██  ██ 
▄█▀▀█ ██▪▐█▐▐▌▄█▀▀█ ▄█▀▀█ ▄█ ▀█▄ ▐█.▪▐█·
▐█ ▪▐▌▐█▌██▐█▌▐█ ▪▐▌▐█ ▪▐▌▐█▄▪▐█ ▐█▌·▐█▌
 ▀  ▀  ▀▀▀▀ ▀▪ ▀  ▀  ▀  ▀ ·▀▀▀▀  ▀▀▀ ▀▀▀
 (Are We All Actually Going To Iran?)

 =============== Express ===============
 Version 1.0
`)

/**
 * 
 * This is the entry point of the Node.js server.
 * 
 * This server is responsible for a few things:
 * 
 *  - Serving the web application
 *  - The API for User Management
 * 
 *  - Sending Queries made by the user to the Database
 *  - Sending Real-Time updates about query progess to the user (using socket.io)
 *  - Serving the XML exports made by the Database
 * 
 *  - Registering the IP adresses of the Raspberry Pis (using API keys)
 *  - Sending these IP adresses to our Telegram Chat via a Telegram Bot.
 * 
 *  - This server will also automatically restart and download dependencies 
 *    whenever a commit was pushed to the GitHub repository.
 *    This is done using a GitHub Webhook.
 */

const path = require('path')
const express = require("express")
const bodyParser = require("body-parser")
const app = express()
const port = 8080

// http server and websocket server:
const http = require("http").Server(app)
const io = require("socket.io")(http)

// The folder containing Database exports:
const exportsFolder = __dirname + "/../../vmdb/db_exports/"
module.exports.exportsFolder = exportsFolder

// Add event listeners to the websockets (can be found in ./socket.js):
require("./socket")(io)

// Parse POST request bodies as JSON:
app.use(bodyParser.json())

// Configure response headers:
app.use((_req, res, next) => {
    res.header("Access-Control-Allow-Origin", "http://localhost:4200")
    res.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Authorization, Range")
    next()
})

// API for registering Raspberry PI IP adresses (and also sending them to the Telegram Chat).
const repsbarrieApi = require("./repsbarrie-api")
// route for registering ip:
app.post("/api/register-repsbarrie-ip", repsbarrieApi.registerIp)
// route for showing ips:
app.get("/api/repsbarries", repsbarrieApi.showIps)

const auth = require("./auth.js")
const passport = require('passport')

// the following routes have to do with Authentication and User Management. (see ./auth.js)
app.use(passport.initialize())
app.post("/api/login", auth.login)
app.post("/api/register", auth.register)
app.get("/api/me", auth.jwt, auth.me)

// Route for the GitHub webhook. (see ./github-webhook.js)
app.post("/api/github-webhook", require("./github-webhook"))

console.log("Exports are saved in?", exportsFolder)
// Serve exports over this route:
app.use("/exports", express.static(exportsFolder))

// The webapp is served over every other route:
app.use("/", express.static(path.join(__dirname, "../awaagular/dist/awaagular/")))
app.use("/*", (req, res) => res.sendFile(path.resolve("../awaagular/dist/awaagular/index.html")))

// Start listening:
http.listen(port, () => console.log("AWAAGTI-express & socket.io svr running on port " + port))

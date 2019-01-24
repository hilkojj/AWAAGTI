const express = require("express")
const bodyParser = require("body-parser")
const app = express()
const port = 8080

const http = require("http").Server(app)
const io = require("socket.io")(http)

const exportsFolder = "/../db/db_exports/"
module.exports.exportsFolder = exportsFolder

require("./socket.js")(io)

app.use(bodyParser.json())
app.use((_req, res, next) => {
    res.header("Access-Control-Allow-Origin", "http://localhost:4200")
    res.header("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Authorization")
    next()
});

const auth = require("./auth.js")
const passport = require('passport')

app.use(passport.initialize())
app.post("/api/login", auth.login)
app.post("/api/register", auth.register)
app.get("/api/me", auth.jwt, auth.me)

console.log(__dirname + exportsFolder)
app.use("/exports", express.static(__dirname + exportsFolder))

http.listen(port, () => console.log("AWAAGTI-express & socket.io svr running on port " + port))

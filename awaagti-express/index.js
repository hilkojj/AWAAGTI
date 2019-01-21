const express = require("express")
const bodyParser = require("body-parser")

const app = express()
const port = 8080

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

app.listen(port, () => console.log("AWAAGTI-express svr running on port " + port))

const express = require("express")
const bodyParser = require("body-parser")

const app = express()
const port = 8080

app.use(bodyParser.json())

const auth = require("./auth.js")
const passport = require('passport')

app.use(passport.initialize())
app.post("/api/login", auth.login)
app.post("/api/register", auth.register)

app.listen(port, () => console.log("AWAAGTI-express svr running on port " + port))

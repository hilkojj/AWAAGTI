const express = require("express")

const app = express()
const port = 8080

const auth = require("./auth.js")

app.listen(port, () => console.log("AWAAGTI-express svr running on port " + port))


const bcrypt = require("bcrypt")
const jwt = require('jsonwebtoken')
const fs = require("fs")
const secret = fs.readFileSync("./secret.txt").toString()

const passport = require('passport');
const LocalStrategy = require('passport-local').Strategy;

const usersFile = "./users.json"

// load users from .json file.
const users = fs.existsSync(usersFile) ? JSON.parse(fs.readFileSync(usersFile).toString()) : {}

passport.use(new LocalStrategy((username, password, done) => {
    
    let user = users[username]
    if (!user)
        done("Username does not exist")

    await bcrypt.compare(password, user.password) ? done()

}))

module.exports.generateJWT = username => jwt.sign({ username }, secret)
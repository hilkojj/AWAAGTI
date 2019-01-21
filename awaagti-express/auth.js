
const bcrypt = require("bcrypt")
const jwt = require('jsonwebtoken')
const fs = require("fs")
const secret = fs.readFileSync("./secret.txt").toString()
const expressJwt = require('express-jwt');

module.exports.jwt = expressJwt({
    secret: secret,
    userProperty: 'payload'
})

const passport = require('passport')
const LocalStrategy = require('passport-local').Strategy

const usersFile = "./users.json"

// load users from .json file.
const users = fs.existsSync(usersFile) ? JSON.parse(fs.readFileSync(usersFile).toString()) : {}

const saveUser = async (username, password) => {
    users[username] = {
        username: username,
        password: await bcrypt.hash(password, 3),
        registeredTimestamp: Date.now()
    }
    console.log("A very new user has regsitered:", username)
    saveUsers()
}

const saveUsers = () =>
    fs.writeFile(usersFile, JSON.stringify(users), e => e && console.error("Error writing users", e))

passport.use(new LocalStrategy({
    usernameField: "username",
    passwordField: "password"
}, async (username, password, done) => {

    let user = users[username]

    if (!user)
        return done(null, null, { message: "Username does not exist" })

    await bcrypt.compare(password, user.password)
        ?
        done(null, user)    // password is correct
        :
        done(null, null, { message: "Passport is wrong" })

}))

const generateJWT = username => jwt.sign({ username }, secret)

module.exports.login = (req, res) => {
    passport.authenticate("local", (err, user, info) => {
        if (err)
            return res.status(500).json(err)

        if (user)
            return res.status(200).json({
                token: generateJWT(user.username)
            })
        else return res.status(401).json(info)

    })(req, res)
}

module.exports.register = async (req, res) => {
    let username = String(req.body.username || "")
    let password = String(req.body.password || "")

    if (!username.length)
        return res.status(400).json({ message: "A username is required" })
    if (username.match(/W+/))
        return res.status(400).json({ message: "A username can only contain normal characters and numbers" })
    if (!password.length)
        return res.status(400).json({ message: "A password is required" })
    if (users[username])
        return res.status(400).json({ message: "Username already exists" })

    await saveUser(username, password)
    module.exports.login(req, res)
}

module.exports.me = async (req, res) => {
    let user = users[req.payload.username]
    res.status(user ? 200 : 401).json(user)
}

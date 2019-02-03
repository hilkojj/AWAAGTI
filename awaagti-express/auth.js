/**
 * This file is responsible for User Management and Authentication using JSON Web Tokens.
 */

const bcrypt = require("bcrypt")
const jwt = require('jsonwebtoken')
const fs = require("fs")

/**
 * The "secret" is a string that is used to encrypt the Json Web Tokens
 * It is stored in the file "./secret.txt" and it should stay out of Git.
 */
const secret = fs.readFileSync("./secret.txt").toString()
const expressJwt = require('express-jwt');

module.exports.jwt = expressJwt({
    secret: secret,
    userProperty: 'payload'
})

module.exports.secret = secret

const passport = require('passport')
const LocalStrategy = require('passport-local').Strategy

const usersFile = "./users.json"

// load users from .json file.
const users = fs.existsSync(usersFile) ? JSON.parse(fs.readFileSync(usersFile).toString()) : {}

module.exports.users = users

/**
 * Registers a new user.
 * @param {string} username 
 * @param {string} password 
 */
const saveUser = async (username, password) => {
    users[username.toLowerCase()] = {
        username: username,
        password: await bcrypt.hash(password, 3),
        registeredTimestamp: Date.now()
    }
    console.log("A very new user has regsitered:", username)
    saveUsers()
}

/**
 * Saves the users to a JSON file so that it can be loaded again whenever the server crashes or restarts.
 */
const saveUsers = () =>
    fs.writeFile(usersFile, JSON.stringify(users), e => e && console.error("Error writing users", e))

module.exports.saveUsers = saveUsers

/**
 * Login using Passport.js authentication middleware.
 */
passport.use(new LocalStrategy({
    usernameField: "username",
    passwordField: "password"
}, async (username, password, done) => {

    let user = users[username.toLowerCase()]

    if (!user)
        return done(null, null, { message: "Username does not exist" })

    await bcrypt.compare(password, user.password)
        ?
        done(null, user)    // password is correct
        :
        done(null, null, { message: "Passport is wrong" })

}))

/**
 * Generates a JSON webtoken (using the secret) that can be sent to the user.
 * @param {string} username 
 */
const generateJWT = username => jwt.sign({ username: username.toLowerCase() }, secret)

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
    if (users[username.toLowerCase()])
        return res.status(400).json({ message: "Username already exists" })

    await saveUser(username, password)
    module.exports.login(req, res)
}

/**
 * Sends user info
 */
module.exports.me = async (req, res) => {
    let user = users[req.payload.username]
    res.status(user ? 200 : 401).json(user)
}

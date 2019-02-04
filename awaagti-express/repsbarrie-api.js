/**
 * This file is responsible for:
 * 
 *  - Registering the IP adresses of the Raspberry Pis (using API keys)
 *  - Sending these IP adresses to our Telegram Chat via a Telegram Bot.
 */

const fs = require("fs")
const apiKeysFile = "./api-keys.json"
const apiKeys = fs.existsSync(apiKeysFile) ? JSON.parse(fs.readFileSync(apiKeysFile).toString()) : {}

const TelegramBot = require('node-telegram-bot-api')
const telegramTokenAndChatId = fs.readFileSync("./telegram-token.txt").toString().split("\n")

const bot = new TelegramBot(telegramTokenAndChatId[0])

const ipsFile = "./ips.json"
const ips = fs.existsSync(ipsFile) ? JSON.parse(fs.readFileSync(ipsFile).toString()) : {}

const bird = `
    \\\\
    (o>
 \\\\_//)
  \\_/_)
   _|_
`
/**
 * The bird will look like this when printed:
 
    \\
    (o>
 \\_//)
  \_/_)
   _|_
 */

module.exports.registerIp = (req, res) => {

    let ip = String(req.body.ip).trim()
    let apiKey = String(req.body.apiKey)

    if (ip.length == 0) return res.send("fuck you pls send your ip pls")

    if (apiKeys[apiKey]) {

        // api key is valid. Raspberry recognized.
        let name = apiKeys[apiKey]
        let prevId = (ips[name] || {}).ip
        ips[name] = {
            ip,
            timeRegisterd: new Date().toTimeString()
        }

        fs.writeFile("./ips.json", JSON.stringify(ips), err => err && console.log(err))

        // Send a friendly message with ascii art of a bird.
        res.send(`${bird}\nThank you ${name} for your IP (${ip})\n`)

        // If the ip has changed -> send it to our Telegram Chat
        prevId != ip && bot.sendMessage(telegramTokenAndChatId[1], `Wowie dit is het IP van ${name}:\n${ip}`)

    } else res.send("fuck you i dont know you\n") // send a not so friendly message when the API key is invalid.
}

module.exports.showIps = (req, res) => res.json(ips)
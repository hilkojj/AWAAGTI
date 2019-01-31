const fs = require("fs")
const apiKeysFile = "./api-keys.json"
const apiKeys = fs.existsSync(apiKeysFile) ? JSON.parse(fs.readFileSync(apiKeysFile).toString()) : {}

const ips = {}

const bird = `
    \\
    (o>
 \\_//)
  \_/_)
   _|_
`

module.exports.registerIp = (req, res) => {

    let ip = String(req.body.ip).trim()
    let apiKey = String(req.body.apiKey)

    if (apiKeys[apiKey]) {

        let name = apiKeys[apiKey]
        ips[name] = {
            ip,
            timeRegisterd: new Date().toTimeString()
        }

        res.send(`${bird}\nThank you ${name} for your IP (${ip})\n`)

    } else res.send("fuck you i dont know you\n")
}

module.exports.showIps = (req, res) => res.json(ips)
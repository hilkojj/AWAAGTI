const exec = require("child_process").exec
const path = require("path")

// this very nice code pulls shit from master and rebuilds shit when someone pushes shit to master
// also <3 callbacks

module.exports = (req, res) => {

    console.log("o wowie someone pushed to master o no")

    exec("sudo git fetch --all; git reset --hard origin/master", { cwd: path.resolve("../") }, (err, stdout, stderr) => {

        console.log(err, stderr, stdout)
        console.log("wowie i have pulled master, now lets install 5 million npm modules")

        exec("sudo npm install", { cwd: path.resolve("../awaagular/") }, () => {

            console.log(err, stderr, stdout)
            console.log("npm modules for angular installed")

            exec("sudo npm install", { cwd: path.resolve("./") }, () => {

                console.log(err, stderr, stdout)
                console.log("npm modules for node installed")
                console.log("node server will restart")
                
                exec("sudo pm2 restart 0")
            })


        })


    })
}
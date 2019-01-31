const exec = require("child_process").exec
const path = require("path")

// this very nice code pulls shit from master and rebuilds shit when someone pushes shit to master
// also <3 callbacks

module.exports = (req, res) => {

    console.log("o wowie someone pushed to master o no")

    exec("git fetch --all; git reset --hard origin/master", { cwd: path.resolve("../") }, (err, stdout, stderr) => {

        console.log(err, stderr, stdout)
        console.log("wowie i have pulled master, now lets install 5 million npm modules")

        exec("npm install", { cwd: path.resolve("../awaagular/") }, () => {

            console.log(err, stderr, stdout)
            console.log("npm modules for angular installed")

            exec("npm install", { cwd: path.resolve("./") }, () => {

                console.log(err, stderr, stdout)
                console.log("npm modules for node installed")

                console.log("lets use 100% CPU (lets build the angular app)")

                console.log("node server will restart before ng build is done")
                
                exec("ng build --prod --aot &", {
                    cwd: path.resolve("../awaagular/")
                })
                exec("sudo pm2 restart 0")
            })


        })


    })
}
const express = require("express")

const app = express()
const port = 8080



testjson = {
    name: "DIT IS MIJN MOOIE EXPORT",
    stationIds: [50, 100, 7900, 7950],
    timeFrame: {"from": 12123123123, "to": 123123123, "interval": 3600},
    what: "temperature",
    sortBy: "temperature",
    limit: "100"
}

var net = require('net');

var client = new net.Socket();
client.connect(12345, '127.0.0.1', function() {
	console.log('Connected');
	client.write(JSON.stringify(testjson));
});

app.listen(port, () => console.log("AWAAGTI-express svr running on port " + port))
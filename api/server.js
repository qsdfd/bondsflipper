
var express = require('express');
var app = express();
var port = 80;
var bodyParser = require('body-parser');
  

app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());


var routes = require('./routes/bondFlipRoutes');
routes(app);


var server = app.listen(port, () => {
    console.log(`API running on port ${port}...`);
    app.emit("appStarted");
});

function stop() {
    server.close();
}

module.exports = server;
module.exports.stop = stop;
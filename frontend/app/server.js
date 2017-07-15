// Get dependencies
const express = require('express');
const path = require('path');
const http = require('http');
var oracledb = require('oracledb');

const config = require('./server/config').Config;

// Get our API routes
const api = require('./server/routes/api');

const app = express();

// Point static path to dist
app.use(express.static(path.join(__dirname, 'dist')));

// Set our api routes
app.use('/api', api);

// Catch all other routes and return the index file
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist/index.html'));
});

oracledb.getConnection(
  {
    user: config.database.user,
    password: config.database.password,
    connectString: config.database.connectString
  },
  function (err, connection) {
    if (err) {
      console.error(err.message);
      return;
    }

    // Run the application
    app.locals.config = config;
    app.locals.db = connection;
    app.listen(config.application.port, () => {
      console.log(`Node.js app is listening at http://localhost:${config.application.port}`);
    });
  }
);

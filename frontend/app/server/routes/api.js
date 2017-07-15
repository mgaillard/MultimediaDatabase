const express = require('express');
var validUrl = require('valid-url');

const router = express.Router();

router.get('/search', function (req, res) {
  const db = req.app.locals.db;
  const app_config = req.app.locals.config;

  const query_url = req.query.url;

  if (validUrl.isUri(query_url)) {
    db.execute("SELECT * FROM (SELECT FILENAME, ImageScore(1) AS distance FROM mdb_insa.Images WHERE ImageDistance(FILENAME, :query_url, 1) >= 0) WHERE ROWNUM <= 12", [query_url], function (err, result) {
      if (err) {
        console.error(err);
        res.setHeader('Content-Type', 'text/plain');
        res.end("Error during the execution of the query", 'utf-8');
        return;
      }

      res.setHeader('Content-Type', 'application/json');
      res.end(JSON.stringify(result), 'utf-8');
    });
  } else {
    res.setHeader('Content-Type', 'text/plain');
    res.end("Error the query URL is not valid.", 'utf-8');
  }
});

module.exports = router;
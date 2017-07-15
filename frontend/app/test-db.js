var oracledb = require('oracledb');
const config = require('./server/config').Config;

oracledb.getConnection(
  {
    user: config.database.user,
    password: config.database.password,
    connectString: config.database.connectString
  },
  function (err, connection) {
    if (err) {
      console.error(err.message);
      process.exit(1);
    }
    process.exit(0);
  }
);
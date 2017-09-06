const express = require('express')
const httpProxy = require('http-proxy')
const proxy = httpProxy.createProxyServer();

const app = express();
app.use('/', express.static('dist'));
app.route('/typebook/*$').all((req, res) => {
  proxy.web(req, res, { target: 'http://typebook:8888/' + req.url.substr('/typebook'.length), ignorePath: true });
});
app.listen(8080, () => {
  console.log('server starts on port 8080');
});

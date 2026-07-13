const http = require('http');
const fs = require('fs');
const path = require('path');

const root = __dirname;
const types = {'.html': 'text/html; charset=utf-8', '.css': 'text/css', '.js': 'text/javascript'};
http.createServer((req, res) => {
  const requested = req.url === '/' ? '/index.html' : req.url;
  const file = path.join(root, path.normalize(requested).replace(/^([.][.][/\\])+/, ''));
  if (!file.startsWith(root)) { res.writeHead(403); return res.end(); }
  fs.readFile(file, (error, data) => {
    if (error) { res.writeHead(404); return res.end('Not found'); }
    res.writeHead(200, {'Content-Type': types[path.extname(file)] ?? 'application/octet-stream'});
    res.end(data);
  });
}).listen(3000, '0.0.0.0', () => console.log('wireframe listening on 3000'));

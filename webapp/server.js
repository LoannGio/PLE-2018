const express = require('express');
const path = require('path');
const app = express();
const logger = require('morgan');
const port = 1337;
const hbase = require('hbase-rpc-client')

let client = hbase({
  zookeeperHosts: 'localhost:8080',
  zookeeperRoot: '/hbase',
  rootRegionZKPath: '/meta-region-server',
  rpcTimeout: 30000,
  pingTimeout: 30000,
  callTimeout: 5000
});

    

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');


app.get('/', function (req, res){  //index
  
  res.render('index', {title: 'BigData Viewer'});
});

app.get('/projet/creer', function (req, res){
  //Envoyer formulaire de creation projet
});

app.post('/projet/creer', function (req, res){
  //Recup infos du formulaire de creation et creer
});

app.get('/projet/list', function (req, res){
  //Liste les projets
});

app.get('/projet/:id', function (req, res){
  let id = req.params.id;
  //Récupérer & afficher projet
});


app.listen(port, function(){
  console.log('Listening on port '+ port);
});
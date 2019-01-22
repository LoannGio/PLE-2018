const express = require('express');
const path = require('path');
const app = express();
const logger = require('morgan');
const port = 1337;
const hbase = require('hbase');

let client = hbase({ host: '10.0.213.28', port: 8080 });

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');


app.get('/', function (req, res){  //index
  res.render('index', {title: 'BigData Viewer'});
  client.table('lgiovannange').row('N42E009.hgt').get('infos', function(err, cell){
    console.log(cell);
  });
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